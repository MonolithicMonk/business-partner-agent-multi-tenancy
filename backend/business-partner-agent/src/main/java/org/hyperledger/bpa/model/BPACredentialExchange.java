/*
 * Copyright (c) 2020-2021 - for information on the respective copyright owner
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
package org.hyperledger.bpa.model;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.AutoPopulated;
import io.micronaut.data.annotation.DateCreated;
import io.micronaut.data.annotation.DateUpdated;
import io.micronaut.data.annotation.TypeDef;
import io.micronaut.data.model.DataType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hyperledger.aries.api.issue_credential_v1.CredentialExchangeRole;
import org.hyperledger.aries.api.issue_credential_v1.CredentialExchangeState;
import org.hyperledger.bpa.api.CredentialType;
import org.hyperledger.bpa.api.aries.ExchangeVersion;

import javax.persistence.*;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Stores issued credentials
 *
 * TODO is basically the same as {@link MyCredential} and both can be merged
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "bpa_credential_exchange")
public class BPACredentialExchange {

    @Id
    @AutoPopulated
    private UUID id;

    @DateCreated
    private Instant createdAt;

    @DateUpdated
    private Instant updatedAt;

    @OneToOne
    private BPASchema schema;

    @OneToOne
    private BPACredentialDefinition credDef;

    @OneToOne
    private Partner partner;

    @Enumerated(EnumType.STRING)
    private CredentialType type;

    @Nullable
    private String label;

    private String threadId;

    private String credentialExchangeId;

    @Enumerated(EnumType.STRING)
    private CredentialExchangeRole role;

    @Enumerated(EnumType.STRING)
    private CredentialExchangeState state;

    @Nullable
    @Enumerated(EnumType.STRING)
    private ExchangeVersion exchangeVersion;

    @Deprecated
    @Nullable
    @TypeDef(type = DataType.JSON)
    private Map<String, Object> credentialOffer;

    @Deprecated
    @Nullable
    @TypeDef(type = DataType.JSON)
    private Map<String, Object> credentialProposal;

    @Nullable
    @TypeDef(type = DataType.JSON)
    private Map<String, Object> credential;

    // revocation - link to issued credential
    /** credential revocation identifier */
    @Nullable
    private String credRevId;
    /** revocation registry identifier */
    @Nullable
    private String revRegId;
    /** if the credential has been revoked */
    @Nullable
    private Boolean revoked;

}
