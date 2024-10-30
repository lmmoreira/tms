package br.com.logistics.tms.company.infrastructure.jpa.entities;

import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

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