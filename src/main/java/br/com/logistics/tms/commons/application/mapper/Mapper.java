package br.com.logistics.tms.commons.application.mapper;

public interface Mapper {

    <S, D> D map(S source, Class<D> clazz);

}
