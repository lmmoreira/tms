package br.com.logistics.tms.company.infrastructure.jpa.entities;

import br.com.logistics.tms.commons.infrastructure.gateways.outbox.AbstractOutboxEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "outbox", schema = "company")
public class CompanyOutboxEntity extends AbstractOutboxEntity {

}