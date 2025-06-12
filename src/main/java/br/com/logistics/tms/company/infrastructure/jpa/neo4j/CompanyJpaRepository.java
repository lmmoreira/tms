package br.com.logistics.tms.company.infrastructure.jpa.neo4j;

import java.util.List;
import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

public interface CompanyJpaRepository extends Neo4jRepository<CompanyEntity, String> {

    @Query("MATCH (c:Company)-[r:PARENT_OF]->(related) WHERE c.companyId = $companyId RETURN c, collect(r), collect(related)")
    Optional<CompanyEntity> findCompanyWithRelations(String companyId);

    @Query("MATCH (c:Company)<-[r:PARENT_OF]-(related) WHERE c.companyId = $companyId RETURN  collect(related)")
    List<CompanyEntity> findCompanyWithIncomingRelations(String companyId);

}
