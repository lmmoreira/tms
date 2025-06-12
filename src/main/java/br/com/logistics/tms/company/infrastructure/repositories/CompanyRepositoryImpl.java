package br.com.logistics.tms.company.infrastructure.repositories;

import br.com.logistics.tms.company.application.repositories.CompanyRepository;
import br.com.logistics.tms.company.domain.Cnpj;
import br.com.logistics.tms.company.domain.Company;
import br.com.logistics.tms.company.domain.CompanyId;
import br.com.logistics.tms.company.infrastructure.jpa.neo4j.CompanyEntity;
import br.com.logistics.tms.company.infrastructure.jpa.pg.CompanyPgEntity;
import br.com.logistics.tms.company.infrastructure.jpa.neo4j.RelationshipEntity;
import br.com.logistics.tms.company.infrastructure.jpa.neo4j.CompanyJpaRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import br.com.logistics.tms.company.infrastructure.jpa.pg.CompanyPgJpaRepository;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
@Slf4j
public class CompanyRepositoryImpl implements CompanyRepository {

    private final CompanyJpaRepository companyJpaRepository;
    private final CompanyPgJpaRepository companyPgJpaRepository;

    @Override
    @Transactional
    public Optional<Company> getCompanyById(CompanyId id) {

        final String uuidShein = "b314b21e-df37-4d1b-ad66-8e26b9ac696c";
        final String uuidShopee = "74e926dc-7b59-4f40-a089-4d4be853e56e";
        final String uuidPerfume = "ca938fbf-f2b8-4b77-b913-78915d60449a";
        final String uuidCorreio = "51022566-6986-46ff-bba5-d3ad20f59a0d";

        CompanyEntity shein = null;
        CompanyEntity shopee = null;
        CompanyEntity perfime = null;
        CompanyEntity correio = null;

        if (companyJpaRepository.findById(uuidShein).isEmpty()) {
            shein = CompanyEntity.builder().name("Shein").id(uuidShein).cnpj("54.713.528/0001-37")
                .build();
            companyJpaRepository.save(shein);
        }

        if (companyJpaRepository.findById(uuidShopee).isEmpty()) {
            shopee = CompanyEntity.builder().name("Shopee").id(uuidShopee)
                .cnpj("13.554.215/0001-04").build();
            companyJpaRepository.save(shopee);
        }

        if (companyJpaRepository.findById(uuidPerfume).isEmpty()) {
            perfime = CompanyEntity.builder().name("Perfume").id(uuidPerfume)
                .cnpj("87.464.841/0001-38").build();
            companyJpaRepository.save(perfime);
        }

        if (companyJpaRepository.findById(uuidCorreio).isEmpty()) {
            correio = CompanyEntity.builder().name("Correio").id(uuidCorreio)
                .cnpj("35.893.532/0001-80").build();
            companyJpaRepository.save(correio);
        }

        CompanyEntity sheinBusca = companyJpaRepository.findById(uuidShein).orElse(null);
        CompanyEntity shopeeBusca = companyJpaRepository.findById(uuidShopee).orElse(null);
        CompanyEntity perfumeBusca = companyJpaRepository.findById(uuidPerfume).orElse(null);
        CompanyEntity correioBUsca = companyJpaRepository.findById(uuidCorreio).orElse(null);

        sheinBusca.setRelation(Set.of(RelationshipEntity.builder()
            .created(LocalDate.now())
            .child(perfumeBusca)
            .numVolumes(10)
            .cuttimes("leonzin")
            .build()));

        shopeeBusca.setRelation(Set.of(RelationshipEntity.builder()
            .created(LocalDate.now())
            .child(perfumeBusca)
            .numVolumes(15)
            .cuttimes("onnnnn")
            .build()));

        perfumeBusca.setRelation(Set.of(RelationshipEntity.builder()
                .created(LocalDate.now())
                .child(correioBUsca)
                .numVolumes(15)
                .cuttimes("08-10")
                .source(sheinBusca.getName())
                .build(),
            RelationshipEntity.builder()
                .created(LocalDate.now())
                .child(correioBUsca)
                .numVolumes(1)
                .cuttimes("01-10")
                .source(shopeeBusca.getName())
                .build()));

        /*companyJpaRepository.save(sheinBusca);
        companyJpaRepository.save(shopeeBusca);
        companyJpaRepository.save(perfumeBusca);
        companyJpaRepository.save(correioBUsca);*/


        //CompanyEntity shein = CompanyEntity.builder().name("Shein").companyId(UUID.randomUUID().toString()).cnpj("123").configurationValue(
        //    Map.of("Config1", "ValueConfig1")).build();

        //CompanyEntity shein = CompanyEntity.builder().name("Shein").companyId(UUID.randomUUID().toString()).cnpj("123").build();
        //    Map.of("Config1", "ValueConfig1")).build();

        //companyJpaRepository.save(shein);



        //sheinBusca = companyJpaRepository.findById(companyId.value().toString()).orElse(null);
        sheinBusca = companyJpaRepository.findCompanyWithRelations(uuidPerfume).orElse(null);
        var parents = companyJpaRepository.findCompanyWithIncomingRelations(uuidPerfume);

        return Optional.of(sheinBusca.toCompany());

    }

    @Override
    public Optional<Company> getCompanyByCnpj(Cnpj cnpj) {
        return Optional.empty();
    }

    @Override
    @WithSpan
    public Company create(Company company) {
        log.info("Creating company: {}", company);
        createPG(company);
        createNeo(company);
        return company;
    }

    @Transactional("jpaTransactionManager")
    public void createPG(Company company) {
        log.info("Creating company in PG: {}", company);

        CompanyPgEntity pgEntity = new CompanyPgEntity(company.companyId().value(),
                company.name(), company.cnpj().value());
        companyPgJpaRepository.save(pgEntity);
    }

    @Transactional("neo4JTransactionManager")
    public Company createNeo(Company company) {
        log.info("Creating company in NEO: {}", company);
        return companyJpaRepository.save(CompanyEntity.of(company)).toCompany();
    }

    @Override
    public Company update(Company company) {
        return null;
    }

}


