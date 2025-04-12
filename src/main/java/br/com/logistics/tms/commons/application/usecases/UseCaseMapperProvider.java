package br.com.logistics.tms.commons.application.usecases;

import br.com.logistics.tms.commons.application.mapper.Mapper;

public class UseCaseMapperProvider {

    private static Mapper mapper;

    private UseCaseMapperProvider() {}

    public static void setMapper(Mapper instance) {
        mapper = instance;
    }

    public static Mapper getMapper() {
        if (mapper == null) {
            throw new IllegalStateException("Mapper not initialized");
        }
        return mapper;
    }
}
