/*
 * Copyright (c) 2020-2022 - for information on the respective copyright owner
 * see the NOTICE file and/or the repository at
 * https://github.com/hyperledger-labs/business-partner-agent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.hyperledger.bpa.tenancy;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.bpa.tenancy.BPATenantSchemaID;
import org.hyperledger.bpa.tenancy.BPATenantSchemaIDRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Singleton
@Requires(notEnv = { Environment.TEST })
public class TenantSchemaIDOperations {

    @Inject
    BPATenantSchemaIDRepository bpaTenantSchemaIDRepository;

    public void createTenantSchemaID(String newTenantSchemaID) {
        bpaTenantSchemaIDRepository.findByTenantSchemaID(newTenantSchemaID)
                .ifPresentOrElse(tenantSchemaID -> {
                    log.info("Tenant schema ID already exists");
                },
                        () -> bpaTenantSchemaIDRepository.save(BPATenantSchemaID.builder()
                                .tenantSchemaID(newTenantSchemaID)
                                .build()));
    }

    public Optional<BPATenantSchemaID> findSchema(String tenantSchemaID) {
        return bpaTenantSchemaIDRepository.findByTenantSchemaID(tenantSchemaID);
    }

    // Add the findAll method
    public List<BPATenantSchemaID> findAll() {
        return bpaTenantSchemaIDRepository.findAll();
    }

}