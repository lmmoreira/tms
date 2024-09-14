package br.com.logistics.tms.company.infrastructure.jpa.repositories;

import br.com.logistics.tms.commons.infrastructure.jpa.repositories.CustomRepository;
import br.com.logistics.tms.company.infrastructure.jpa.entities.RelationshipConfigurationEntity;
import java.util.UUID;

public interface RelationshipConfigurationJpaJpaRepository extends
    CustomRepository<RelationshipConfigurationEntity, UUID>,
    RelationshipConfigurationJpaCustomRepository {

}
