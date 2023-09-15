package org.hyperledger.bpa.tenancy.acapy.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.micronaut.data.annotation.AutoPopulated;
import io.micronaut.data.annotation.DateCreated;
import io.micronaut.data.annotation.DateUpdated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.validation.constraints.NotEmpty;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "acapy_tenant_config")
public class AcapyTenantConfig {

    @Id
    @AutoPopulated
    @JsonIgnore
    private UUID id;

    @DateCreated
    private Instant createdAt;

    @DateUpdated
    private Instant updatedAt;
        
    @NotEmpty
    private String walletId;

    @NotEmpty
    private String bearerToken;

    @NotEmpty
    private String tenantId;

}
