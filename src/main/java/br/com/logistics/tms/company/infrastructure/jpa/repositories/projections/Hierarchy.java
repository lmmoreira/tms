package br.com.logistics.tms.company.infrastructure.jpa.repositories.projections;

import br.com.logistics.tms.commons.infrastructure.jpa.entities.JsonToMapConverter;
import java.util.Map;
import java.util.UUID;

public record Hierarchy(
    UUID id,
    UUID parentId,
    UUID childId,
    UUID relationshipConfigurationParentId,
    String configurationKey,
    String configurationValue,
    String parentName,
    String childName
) {

    public Map<String, Object> getConfigurationValue() {
        return new JsonToMapConverter().convertToEntityAttribute(configurationValue);
    }

}
