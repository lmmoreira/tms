package br.com.logistics.tms.company.domain;

import java.util.Map;
import java.util.Set;

public record Configuration(ConfigurationEnum configuration, Map<String, Object> value) {

}
