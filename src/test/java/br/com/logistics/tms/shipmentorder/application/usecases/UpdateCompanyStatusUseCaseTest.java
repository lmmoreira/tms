package br.com.logistics.tms.shipmentorder.application.usecases;

import br.com.logistics.tms.builders.domain.shipmentorder.CompanyBuilder;
import br.com.logistics.tms.commons.domain.Status;
import br.com.logistics.tms.shipmentorder.domain.Company;
import br.com.logistics.tms.shipmentorder.domain.CompanyId;
import br.com.logistics.tms.shipmentorder.application.repositories.CompanyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateCompanyStatusUseCaseTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private UpdateCompanyStatusUseCase useCase;

    @Test
    void shouldUpdateCompanyStatusToDeleted() {
        final UUID companyId = UUID.randomUUID();
        final Company company = CompanyBuilder.aCompany()
                .withCompanyId(companyId)
                .build();

        when(companyRepository.findById(CompanyId.with(companyId)))
                .thenReturn(Optional.of(company));

        useCase.execute(new UpdateCompanyStatusUseCase.Input(companyId, 'D'));

        verify(companyRepository).save(argThat(updatedCompany -> updatedCompany.getStatus().isDeleted()));
    }

    @Test
    void shouldUpdateCompanyStatusToSuspended() {
        final UUID companyId = UUID.randomUUID();
        final Company company = CompanyBuilder.aCompany()
                .withCompanyId(companyId)
                .build();

        when(companyRepository.findById(CompanyId.with(companyId)))
                .thenReturn(Optional.of(company));

        useCase.execute(new UpdateCompanyStatusUseCase.Input(companyId, 'S'));

        verify(companyRepository).save(argThat(updatedCompany -> updatedCompany.getStatus().isSuspended()));
    }

    @Test
    void shouldUpdateCompanyStatusToActive() {
        final UUID companyId = UUID.randomUUID();
        final Company company = CompanyBuilder.aCompany()
                .withCompanyId(companyId)
                .build();

        when(companyRepository.findById(CompanyId.with(companyId)))
                .thenReturn(Optional.of(company));

        useCase.execute(new UpdateCompanyStatusUseCase.Input(companyId, 'A'));

        verify(companyRepository).save(argThat(updatedCompany -> updatedCompany.getStatus().isActive()));
    }

    @Test
    void shouldNotFailIfCompanyDoesNotExist() {
        final UUID companyId = UUID.randomUUID();

        when(companyRepository.findById(CompanyId.with(companyId)))
                .thenReturn(Optional.empty());

        useCase.execute(new UpdateCompanyStatusUseCase.Input(companyId, 'D'));

        verify(companyRepository, never()).save(any());
    }

    @Test
    void shouldPreserveCompanyDataOnStatusUpdate() {
        final UUID companyId = UUID.randomUUID();
        final Company company = CompanyBuilder.aCompany()
                .withCompanyId(companyId)
                .withData(Map.of("types", "[\"SELLER\"]"))
                .build();

        when(companyRepository.findById(CompanyId.with(companyId)))
                .thenReturn(Optional.of(company));

        useCase.execute(new UpdateCompanyStatusUseCase.Input(companyId, 'S'));

        verify(companyRepository).save(argThat(updatedCompany ->
                updatedCompany.getStatus().isSuspended() &&
                updatedCompany.getData().value().containsKey("types")
        ));
    }
}
