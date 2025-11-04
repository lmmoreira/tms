package br.com.logistics.tms.company.infrastructure.dto;

import br.com.logistics.tms.company.domain.CompanyType;
import java.util.Map;
import java.util.Set;

public record CreateCompanyDTO(String name, String cnpj, Set<CompanyType> types, Map<String, Object> configuration) {

}