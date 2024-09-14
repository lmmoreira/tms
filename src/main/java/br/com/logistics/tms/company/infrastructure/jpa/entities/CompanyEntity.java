package br.com.logistics.tms.company.infrastructure.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "company", schema = "company")
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Builder
public class CompanyEntity {

    @Id
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

}