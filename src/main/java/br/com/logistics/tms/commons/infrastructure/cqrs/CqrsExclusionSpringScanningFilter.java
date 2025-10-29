package br.com.logistics.tms.commons.infrastructure.cqrs;

import br.com.logistics.tms.commons.application.annotation.DatabaseRole;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import java.util.Map;

public class CqrsExclusionSpringScanningFilter implements TypeFilter, EnvironmentAware {

    private Environment env;

    @Override
    public void setEnvironment(final Environment environment) {
        this.env = environment;
    }

    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) {
        final AnnotationMetadata am = metadataReader.getAnnotationMetadata();

        final Map<String, Object> attrs = am.getAnnotationAttributes("br.com.logistics.tms.commons.application.annotation.Cqrs");
        if (attrs == null) return false;

        final DatabaseRole annotatedValue = (DatabaseRole) attrs.get("value");
        final String mode = env.getProperty("app.cqrs.mode", "both");

        if ("both".equals(mode)) return false;
        if ("read".equals(mode) && DatabaseRole.READ.equals(annotatedValue)) return false;
        if ("write".equals(mode) && DatabaseRole.WRITE.equals(annotatedValue)) return false;

        return true;
    }
}
