package br.com.logistics.tms.commons.infrastructure.mapper;

import br.com.logistics.tms.commons.application.mapper.Mapper;
import br.com.logistics.tms.commons.application.usecases.UseCaseMapperProvider;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class MapperAdapterImpl implements Mapper {

    private final ObjectMapper objectMapper;

    public MapperAdapterImpl() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        UseCaseMapperProvider.setMapper(this);
    }

    @Override
    public <S, D> D map(S source, Class<D> clazz) {
        return objectMapper.convertValue(source, clazz);
    }
}
