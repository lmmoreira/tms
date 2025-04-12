package br.com.logistics.tms.commons.application.mapper;

@FunctionalInterface
public interface Mapper {

    <S, D> D map(S source, Class<D> clazz);

}
