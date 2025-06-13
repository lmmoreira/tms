package br.com.logistics.tms.commons.infrastructure.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenTelemetryConfiguration {

    private final OpenTelemetry openTelemetry;

    @Autowired
    public OpenTelemetryConfiguration(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
    }

    @Bean
    public Tracer getTracer() {
        return openTelemetry.getTracer("tmsTracer");
    }

    @Bean
    public Meter getMeter() {
        return openTelemetry.getMeter("tmsMeter");
    }

}