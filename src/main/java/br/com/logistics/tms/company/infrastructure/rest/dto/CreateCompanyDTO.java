package br.com.logistics.tms.company.infrastructure.rest.dto;

import java.util.Set;

public record CreateCompanyDTO(String name, String cnpj, Set<String> types) {

}