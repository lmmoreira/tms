package br.com.logistics.tms.company.infrastructure.jpa.repositories;

import br.com.logistics.tms.company.infrastructure.jpa.repositories.projections.Hierarchy;
import java.util.Set;
import java.util.UUID;

public interface RelationshipConfigurationJpaCustomRepository {

    Set<Hierarchy> findAscendantHierarchyById(UUID companyId);

    Set<Hierarchy> findDescendentHierarchyById(UUID companyId);
}
