package br.com.logistics.tms.commons.infrastructure.context;

import java.util.Set;

public record User(String name, String email, Set<String> roles) {

}
