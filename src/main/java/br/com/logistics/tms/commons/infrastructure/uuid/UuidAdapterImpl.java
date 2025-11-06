package br.com.logistics.tms.commons.infrastructure.uuid;

import br.com.logistics.tms.commons.domain.id.DomainUuidProvider;
import br.com.logistics.tms.commons.domain.id.UuidAdapter;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.UUIDUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Lazy(false)
public class UuidAdapterImpl implements UuidAdapter {

    public UuidAdapterImpl() {
        DomainUuidProvider.setUuidAdapter(this);
    }

    @Override
    public UUID generate() {
        return Generators.timeBasedEpochGenerator().generate();
    }

    @Override
    public UUID fromString(String value) {
        return UUIDUtil.uuid(value);
    }
}
