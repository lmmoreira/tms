package br.com.logistics.tms.company.infrastructure.jpa.repositories;

import br.com.logistics.tms.commons.infrastructure.jpa.repositories.sql.SqlFileReader;
import br.com.logistics.tms.company.infrastructure.jpa.repositories.projections.Hierarchy;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("unchecked")
public class RelationshipConfigurationJpaCustomRepositoryImpl implements
    RelationshipConfigurationJpaCustomRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Set<Hierarchy> findAscendantHierarchyById(UUID companyId) {
        final String nativeQuery = SqlFileReader.readSqlFile(
            "company/infrastructure/jpa/repositories/FIND_ASCENDANT_HIERARCHY_BY_ID.sql");
        final Query query = entityManager.createNativeQuery(nativeQuery, Hierarchy.class);
        query.setParameter("companyId", companyId);
        return new HashSet<Hierarchy>(query.getResultList());
    }

    @Override
    public Set<Hierarchy> findDescendentHierarchyById(UUID companyId) {
        String nativeQuery = SqlFileReader.readSqlFile(
            "company/infrastructure/jpa/repositories/FIND_DESCENDENT_HIERARCHY_BY_ID.sql");
        Query query = entityManager.createNativeQuery(nativeQuery, Hierarchy.class);
        query.setParameter("companyId", companyId);
        return new HashSet<Hierarchy>(query.getResultList());
    }
}