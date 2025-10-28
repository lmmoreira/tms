package br.com.logistics.tms.company.infrastructure.spi.dto;

import java.util.UUID;

public record CompanyDTO(UUID companyId,
                         String name,
                         String cnpj) {

}