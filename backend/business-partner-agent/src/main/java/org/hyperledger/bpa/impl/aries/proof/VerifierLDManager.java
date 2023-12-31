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
package org.hyperledger.bpa.impl.aries.proof;

import io.micronaut.context.annotation.Value;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.hyperledger.acy_py.generated.model.DIFOptions;
import org.hyperledger.aries.api.jsonld.ProofType;
import org.hyperledger.aries.api.present_proof_v2.DIFField;
import org.hyperledger.aries.api.present_proof_v2.V2DIFProofRequest;
import org.hyperledger.bpa.api.aries.SchemaAPI;
import org.hyperledger.bpa.impl.aries.jsonld.LDContextResolver;
import org.hyperledger.bpa.impl.aries.schema.SchemaService;
import org.hyperledger.bpa.impl.util.AriesStringUtil;
import org.hyperledger.bpa.persistence.model.BPAProofTemplate;
import org.hyperledger.bpa.persistence.model.prooftemplate.BPAAttributeGroup;
import org.hyperledger.bpa.persistence.model.prooftemplate.BPACondition;
import org.hyperledger.bpa.persistence.model.prooftemplate.BPASchemaRestrictions;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class VerifierLDManager extends BaseLDManager {

    @Value("${bpa.did.prefix}")
    String didPrefix;

    @Inject
    SchemaService schemaService;

    @Inject
    LDContextResolver ctx;

    public V2DIFProofRequest prepareRequest(@NonNull BPAProofTemplate proofTemplate) {
        return V2DIFProofRequest.builder()
                .options(DIFOptions.builder()
                        .challenge(UUID.randomUUID().toString())
                        .domain(UUID.randomUUID().toString())
                        .build())
                .presentationDefinition(V2DIFProofRequest.PresentationDefinition.builder()
                        .id(UUID.randomUUID())
                        .name(proofTemplate.getName())
                        .format(V2DIFProofRequest.PresentationDefinition.ClaimFormat.builder()
                                .ldpVp(V2DIFProofRequest.PresentationDefinition.ClaimFormat.LdpVp.builder()
                                        .addProofType(ProofType.Ed25519Signature2018)
                                        .build())
                                .build())
                        .inputDescriptors(proofTemplate.streamAttributeGroups()
                                .map(this::groupToDescriptor)
                                .collect(Collectors.toList()))
                        .build())
                .build();
    }

    private V2DIFProofRequest.PresentationDefinition.InputDescriptors groupToDescriptor(BPAAttributeGroup group) {
        Map<UUID, DIFField> fields = buildDifFieldsFromGroup(group);
        SchemaAPI schemaAPI = schemaService.getSchema(group.getSchemaId()).orElseThrow();
        String expandedType = schemaAPI.getExpandedType() == null
                ? ctx.resolve(schemaAPI.getSchemaId(), schemaAPI.getLdType())
                : schemaAPI.getExpandedType();
        return V2DIFProofRequest.PresentationDefinition.InputDescriptors
                .builder()
                .id(schemaAPI.getSchemaId())
                .name(schemaAPI.getLabel())
                .schema(List
                        .of(V2DIFProofRequest.PresentationDefinition.InputDescriptors.SchemaInputDescriptorUri.builder()
                                .uri(expandedType)
                                .build()))
                .constraints(V2DIFProofRequest.PresentationDefinition.Constraints.builder()
                        .isHolder(buildDifHolder(fields.keySet()))
                        .fields(List.copyOf(fields.values()))
                        .build())
                .build();
    }

    private Map<UUID, DIFField> buildDifFieldsFromGroup(@NonNull BPAAttributeGroup group) {
        List<BPASchemaRestrictions> restrictions = Objects.requireNonNullElse(group.getSchemaLevelRestrictions(),
                List.of());
        Map<UUID, DIFField> issuerRestrictions = restrictions
                .stream()
                .map(BPASchemaRestrictions::getIssuerDid)
                .filter(StringUtils::isNotEmpty)
                .map(issuerDid -> pair("issuer", DIFField.Filter.builder()
                        // TODO anoncreds need unqualified DIDs,
                        // dif needs it qualified, currently the UI model always strips the qualifier
                        // so we add it again. This will not work with did:indy
                        ._const(AriesStringUtil.qualifyDidIfNeeded(issuerDid, didPrefix))
                        .build()))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));

        Map<String, BPACondition> nameToCondition = group.nameToCondition();
        Map<UUID, DIFField> fieldRestrictions = nameToCondition.entrySet()
                .stream()
                .map(e -> pair(e.getKey(), e.getValue() != null ? e.getValue().toDifFieldFilter() : null))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));

        return Stream.of(issuerRestrictions, fieldRestrictions)
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue));
    }
}
