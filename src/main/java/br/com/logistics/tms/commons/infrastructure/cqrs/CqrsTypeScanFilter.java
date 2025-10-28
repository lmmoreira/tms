package br.com.logistics.tms.commons.infrastructure.cqrs;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import java.util.Locale;
import java.util.Map;

public class CqrsTypeScanFilter implements TypeFilter, EnvironmentAware {

    private Environment env;

    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
    }

    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) {
        final AnnotationMetadata am = metadataReader.getAnnotationMetadata();

        final Map<String, Object> attrs = am.getAnnotationAttributes("br.com.logistics.tms.commons.application.annotation.Cqrs");
        if (attrs == null) return false;

        final String annotatedValue = String.valueOf(attrs.get("value")).toUpperCase(Locale.ROOT);
        final String mode = env.getProperty("app.cqrs.mode", "both").toLowerCase(Locale.ROOT);

        if ("both".equals(mode)) return false;
        if ("read".equals(mode) && "READ".equals(annotatedValue)) return false;
        if ("write".equals(mode) && "WRITE".equals(annotatedValue)) return false;

        return true;
    }
}
