package org.hyperledger.bpa.tenancy;

import org.hyperledger.bpa.tenancy.BPATenantSchemaIDRepository;
import org.hyperledger.bpa.tenancy.TenantSchemaIDOperations;
import org.hyperledger.bpa.tenancy.BPATenantSchemaID;

import org.flywaydb.core.Flyway;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

import jakarta.transaction.Transactional;

// CHANGE (REVERT)
@Singleton
@Slf4j
public class TenantMigrationService {

    @Inject
    private DataSource dataSource;

    @Inject
    BPATenantSchemaIDRepository bpaTenantSchemaIDRepository;

    @Inject
    TenantSchemaIDOperations tenantSchemaIDOperations;

    @Transactional
    public void migrateTenant(String tenantSchemaName) {
        // String targetMigrationString = "1.35";
        // String targetMigrationPrefix = "V";
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas(tenantSchemaName)
                .locations("classpath:databasemigrations")
                .placeholders(new HashMap<String, String>() {
                    {
                        put("my_schema", tenantSchemaName);
                    }
                })
                .load();
        flyway.migrate();
    }

    /**
     * Handles migrations for tenant schemas on application startup.
     *
     * <ul>
     * <li>Migrates the system tenant ("internaloperations").</li>
     * <li>For other existing tenants in the database, it applies migrations. Skips
     * "internaloperations" if already done.</li>
     * <li>If no tenants are identified, logs the existing tenant names.</li>
     * </ul>
     *
     * Usage: Call this method to ensure migrations for all tenants.
     * tenantMigrationService.createTenantSchemaMigrations();
     */
    public void createTenantSchemaMigrations() {
        // Run the migration for the system tenant ("internaloperations") first
        log.info("Running migration for the system tenant: internaloperations");
        migrateTenant("internaloperations");

        List<BPATenantSchemaID> tenantSchemas = tenantSchemaIDOperations.findAll();
        if (!tenantSchemas.isEmpty()) {
            for (BPATenantSchemaID tenantSchema : tenantSchemas) {
                String tenantName = tenantSchema.getTenantSchemaID();
                if (!"internaloperations".equals(tenantName)) {
                    log.info("Running migration for tenant: {}", tenantName);
                    migrateTenant(tenantName);
                } else {
                    log.info("Tenant: {} already migrated, skipping.", tenantName);
                }
            }
        } else {
            log.info("No new migrations necessary. Existing tenants:");
            tenantSchemas.forEach(tenantSchema -> log.info("- {}", tenantSchema.getTenantSchemaID()));
        }
    }
}
