package org.hyperledger.bpa.tenancy.acapy.persistence;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import org.hyperledger.bpa.tenancy.acapy.persistence.AcapyTenantConfig;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface AcapyTenantConfigRepository extends CrudRepository<AcapyTenantConfig, UUID> {

    void updateWalletId(@Id UUID id, @Nullable String walletId);
    void updateBearerToken(@Id UUID id, @Nullable String bearerToken);

    @Query("SELECT wallet_id FROM acapy_tenant_config LIMIT 1")
    Optional<String> findByWalletId();

    @Query("SELECT bearer_token FROM acapy_tenant_config LIMIT 1")
    Optional<String> findByBearerToken();

    Optional<AcapyTenantConfig> getByWalletId(String walletId);

    Optional<AcapyTenantConfig> getByBearerToken(String bearerToken);

}
