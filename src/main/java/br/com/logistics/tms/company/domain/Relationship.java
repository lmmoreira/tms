package br.com.logistics.tms.company.domain;

public record Relationship(Company from,
                           Company to,
                           Company source,
                           Configuration configuration) {

}

