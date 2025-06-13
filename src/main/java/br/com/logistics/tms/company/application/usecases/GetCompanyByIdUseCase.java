package br.com.logistics.tms.company.application.usecases;

import br.com.logistics.tms.commons.application.annotation.DomainService;
import br.com.logistics.tms.commons.application.usecases.UseCase;
import br.com.logistics.tms.company.application.repositories.CompanyRepository;
import br.com.logistics.tms.company.domain.Company;
import br.com.logistics.tms.company.domain.CompanyId;
import br.com.logistics.tms.company.domain.Relationship;
import br.com.logistics.tms.company.domain.Type;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@DomainService
public class GetCompanyByIdUseCase implements UseCase<GetCompanyByIdUseCase.Input, GetCompanyByIdUseCase.Output> {

    private CompanyRepository companyRepository;

    public GetCompanyByIdUseCase(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Output execute(final Input input) {
        return new Output(
            Output.ofCompany(companyRepository.getCompanyById(CompanyId.with(input.companyId()))
                .orElseThrow(() -> new RuntimeException("Company not found"))));
    }

    public record Input(String companyId) {

    }

    public record Output(OutputCompany company) {

        public static OutputCompany ofCompany(Company company) {

            return new OutputCompany(
                company.companyId().value().toString(),
                company.name(),
                company.cnpj().value(),
                company.types().stream().map(Type::toString).collect(Collectors.toSet()),
                new OutputConfiguration(company.configuration().value()),
                company.outgoingPaths().stream().map(Output::ofRelationship)
                    .collect(Collectors.toSet()),
                company.incomingPaths().stream().map(Output::ofRelationship)
                    .collect(Collectors.toSet())
            );
        }

        private static OutputRelationship ofRelationship(Relationship relationship) {
            return new OutputRelationship(
                ofCompany(relationship.from()),
                ofCompany(relationship.to()),
                ofCompany(relationship.source()),
                new OutputConfiguration(relationship.configuration().value())
            );
        }

        public record OutputCompany(String companyId,
                                    String name,
                                    String cnpj,
                                    Set<String> types,
                                    OutputConfiguration configuration,
                                    Set<OutputRelationship> outgoingPaths,
                                    Set<OutputRelationship> incomingPaths) {

        }

        public record OutputConfiguration(Map<String, Object> value) {

        }

        public record OutputRelationship(OutputCompany from,
                                         OutputCompany to,
                                         OutputCompany source,
                                         OutputConfiguration configuration) {

        }
    }

}
