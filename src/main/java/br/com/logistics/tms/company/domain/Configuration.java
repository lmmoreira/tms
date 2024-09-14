package br.com.logistics.tms.company.domain;

import java.util.Map;

public record Configuration(String key, Map<String, Object> value) {

}