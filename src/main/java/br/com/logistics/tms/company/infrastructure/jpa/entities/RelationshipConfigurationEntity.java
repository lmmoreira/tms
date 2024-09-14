package br.com.logistics.tms.company.infrastructure.jpa.entities;

import br.com.logistics.tms.commons.infrastructure.jpa.entities.JsonToMapConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Map;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "relationship_configuration", schema = "company")
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class RelationshipConfigurationEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false)
    private CompanyEntity parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id")
    private CompanyEntity child;

    @Column(name = "configuration_key", nullable = false)
    private String configurationKey;

    @Column(name = "configuration_value", nullable = false, columnDefinition = "jsonb")
    @Convert(converter = JsonToMapConverter.class)
    private Map<String, Object> configurationValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relationship_configuration_parent_id")
    private RelationshipConfigurationEntity parentConfiguration;
}
