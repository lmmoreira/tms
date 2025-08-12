package br.com.logistics.tms.commons.infrastructure.mapper;

import br.com.logistics.tms.commons.application.mapper.Mapper;
import br.com.logistics.tms.commons.application.usecases.UseCaseMapperProvider;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Component
@Lazy(false)
public class MapperAdapterImpl implements Mapper {

    private final ObjectMapper objectMapper;

    public MapperAdapterImpl() {
        this.objectMapper = new ObjectMapper().registerModule(new BlackbirdModule());
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.setSerializationInclusion(NON_NULL);
        UseCaseMapperProvider.setMapper(this);
    }

    @Override
    public <S, D> D map(S source, Class<D> clazz) {
        return objectMapper.convertValue(source, clazz);
    }
}
