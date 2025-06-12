package br.com.logistics.tms.company.infrastructure.jpa.neo4j;

import lombok.Builder;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import java.time.LocalDate;

@RelationshipProperties
@Builder
public class RelationshipEntity {

    @Id
    @GeneratedValue
    private String id;

    private LocalDate created;

    @TargetNode
    private CompanyEntity child;

    private String cuttimes;

    private String source;

    private int numVolumes;

}