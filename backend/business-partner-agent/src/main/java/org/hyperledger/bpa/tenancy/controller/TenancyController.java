package org.hyperledger.bpa.tenancy.controller;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.validation.Validated;
import jakarta.inject.Inject;

import org.hyperledger.bpa.tenancy.TenantMigrationService;
import org.hyperledger.bpa.tenancy.TenantSchemaIDOperations;
import org.hyperledger.bpa.tenancy.acapy.persistence.AcapyTenantConfigOperations;
import org.hyperledger.bpa.tenancy.security.roles.RolesandPermissions;

import lombok.extern.slf4j.Slf4j;
import lombok.Data;

@Slf4j
@Controller
@Validated
@Secured(SecurityRule.IS_AUTHENTICATED)
@ExecuteOn(TaskExecutors.IO)
public class TenancyController {

  @Inject
  TenantMigrationService tenantMigrationService;

  @Inject
  TenantSchemaIDOperations tenantSchemaIDOperations;

  @Inject
  AcapyTenantConfigOperations acapyTenantConfigOperations;

  public static final String TENANCY_CONTROLLER_PATH = "/tenantid";

  @Data
  public static class TenantSchemaRequest {
      private String tenantId;
      private String walletId;
      private String bearerToken;
  }

  @Secured({ RolesandPermissions.ROLE_TENANT_ADMIN })
  @Post(TENANCY_CONTROLLER_PATH)
  public HttpResponse<String> addTenantSchema(@Body TenantSchemaRequest request) {
      String tenantId = request.getTenantId();
      try {
          log.info("Received request to add and migrate tenant: {}", tenantId);

          tenantSchemaIDOperations.createTenantSchemaID(tenantId);
          log.debug("Tenant schema ID created for tenant: {}", tenantId);

          tenantMigrationService.migrateTenant(tenantId);
          log.debug("Migration completed for tenant: {}", tenantId);

          acapyTenantConfigOperations.createAcapyTenantConfig(request.getWalletId(), request.getBearerToken());
          log.debug("Acapy tenant configuration created for tenant: {}", tenantId);

          log.info("Migration successful for tenant: {}", tenantId);
          return HttpResponse.ok("Migration successful for tenant: " + tenantId);
      } catch (Exception e) {
          log.error("Migration failed for tenant: {}. Error: {}", tenantId, e.getMessage(), e);
          return HttpResponse.serverError("Migration failed for tenant: " + tenantId + ". Error: " + e.getMessage());
      }
  }
}
