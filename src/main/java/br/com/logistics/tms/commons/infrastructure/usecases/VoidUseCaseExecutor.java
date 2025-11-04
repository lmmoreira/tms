package br.com.logistics.tms.commons.infrastructure.usecases;

import br.com.logistics.tms.commons.application.usecases.UseCaseExecutor;
import br.com.logistics.tms.commons.application.usecases.UseCaseInterceptor;
import br.com.logistics.tms.commons.application.usecases.VoidUseCase;
import br.com.logistics.tms.commons.application.usecases.VoidUseCaseBuilder;
import br.com.logistics.tms.commons.infrastructure.database.transaction.Transactional;
import br.com.logistics.tms.commons.infrastructure.telemetry.Logable;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class VoidUseCaseExecutor {

    private final Logable logger;
    private final Transactional transactional;

    public VoidUseCaseExecutor(Logable logger,
                               Transactional transactional) {
        this.transactional = transactional;
        this.logger = logger;
    }

    public <INPUT> VoidUseCaseBuilder<INPUT> from(VoidUseCase<INPUT> useCase) {
        return UseCaseExecutor.from(useCase)
                .addInterceptor(startLoggingInterceptor(useCase.getClass()))
                .addInterceptor(((UseCaseInterceptor) logger))
                .addInterceptor(((UseCaseInterceptor) transactional))
                .onException(e -> logger.error(getClass(), "UseCase failed", e));
    }

    private UseCaseInterceptor startLoggingInterceptor(Class<?> clazz) {
        return new UseCaseInterceptor() {
            @Override
            public <T> T intercept(Supplier<T> next) {
                logger.info(clazz, "Executing use case: " + clazz.getSimpleName());
                return next.get();
            }
        };
    }

}