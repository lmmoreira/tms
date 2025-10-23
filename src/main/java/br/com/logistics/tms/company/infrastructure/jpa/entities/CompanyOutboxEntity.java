package br.com.logistics.tms.company.infrastructure.jpa.entities;

import br.com.logistics.tms.commons.infrastructure.gateways.outbox.AbstractOutboxEntity;
import br.com.logistics.tms.company.infrastructure.config.CompanySchema;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "outbox", schema = CompanySchema.COMPANY_SCHEMA)
public class CompanyOutboxEntity extends AbstractOutboxEntity {

}