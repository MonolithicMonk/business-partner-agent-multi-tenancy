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
package org.hyperledger.bpa.tenancy.acapy.persistence;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.bpa.tenancy.acapy.persistence.AcapyTenantConfig;
import org.hyperledger.bpa.tenancy.acapy.persistence.AcapyTenantConfigRepository;

import java.util.Optional;

@Slf4j
@Singleton
@Requires(notEnv = { Environment.TEST })
public class AcapyTenantConfigOperations {

    @Inject
    AcapyTenantConfigRepository acapyTenantConfigRepository;

    public void createAcapyTenantConfig(String newWalletId, String newBearerToken) {
        acapyTenantConfigRepository.getByWalletId(newWalletId)
                .ifPresentOrElse(wallet -> {
                    log.info("Wallet ID already exists");
                },
                () -> acapyTenantConfigRepository.save(AcapyTenantConfig.builder()
                        .walletId(newWalletId)
                        .bearerToken(newBearerToken)
                        .build()));
    }

    public Optional<AcapyTenantConfig> findWalletId(String walletId) {
        return acapyTenantConfigRepository.getByWalletId(walletId);
    }

    public Optional<AcapyTenantConfig> findBearerToken(String bearerToken) {
        return acapyTenantConfigRepository.getByBearerToken(bearerToken);
    }
}
