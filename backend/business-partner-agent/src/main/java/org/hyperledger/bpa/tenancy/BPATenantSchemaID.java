package org.hyperledger.bpa.tenancy;

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
@Table(name = "tenant_schema_id", schema = "internaloperations")
public class BPATenantSchemaID {

    @Id
    @AutoPopulated
    @JsonIgnore
    private UUID id;

    @DateCreated
    private Instant createdAt;

    @DateUpdated
    private Instant updatedAt;

    @NotEmpty
    @Column(name = "tenant_schema_id")
    private String tenantSchemaID;

}
