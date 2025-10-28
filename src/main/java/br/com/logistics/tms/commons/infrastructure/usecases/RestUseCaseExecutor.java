package br.com.logistics.tms.commons.infrastructure.usecases;

import br.com.logistics.tms.commons.application.usecases.*;
import br.com.logistics.tms.commons.infrastructure.cqrs.CqrsAnnotationUtils;
import br.com.logistics.tms.commons.infrastructure.database.routing.DataSourceContextHolder;
import br.com.logistics.tms.commons.infrastructure.database.transaction.TransactionContextHolder;
import br.com.logistics.tms.commons.infrastructure.database.transaction.Transactional;
import br.com.logistics.tms.commons.infrastructure.presenters.rest.DefaultRestPresenter;
import br.com.logistics.tms.commons.infrastructure.telemetry.Logable;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class RestUseCaseExecutor {

    private final DefaultRestPresenter defaultRestPresenter;
    private final Logable logger;
    private final Transactional transactional;

    public RestUseCaseExecutor(DefaultRestPresenter defaultRestPresenter,
                               Logable logger,
                               Transactional transactional) {
        this.defaultRestPresenter = defaultRestPresenter;
        this.transactional = transactional;
        this.logger = logger;
    }

    public <INPUT, OUTPUT> UseCaseBuilder<INPUT, OUTPUT> from(UseCase<INPUT, OUTPUT> useCase) {
        return UseCaseExecutor.from(useCase)
                .presentWith(defaultRestPresenter)
                .addInterceptor(startLoggingInterceptor(useCase.getClass()))
                .addInterceptor(((UseCaseInterceptor) logger))
                .addInterceptor(getReadOnlyInterceptor(useCase))
                .addInterceptor(((UseCaseInterceptor) transactional))
                .onException(e -> logger.error(getClass(), "UseCase failed", e));
    }

    public <INPUT> VoidUseCaseBuilder<INPUT> from(VoidUseCase<INPUT> useCase) {
        return UseCaseExecutor.from(useCase)
                .addInterceptor(startLoggingInterceptor(useCase.getClass()))
                .addInterceptor(((UseCaseInterceptor) logger))
                .addInterceptor(getReadOnlyInterceptor(useCase))
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

    private UseCaseInterceptor getReadOnlyInterceptor(Object useCase) {
        return new UseCaseInterceptor() {
            @Override
            public <T> T intercept(Supplier<T> next) {

                if (CqrsAnnotationUtils.determineReadOnlyFromCqrsAnnotation(useCase)) {
                    DataSourceContextHolder.markAsReadOnly();
                    TransactionContextHolder.markAsReadOnly();
                } else {
                    DataSourceContextHolder.markAsWrite();
                    TransactionContextHolder.markAsReadWrite();
                }

                try {
                    return next.get();
                } finally {
                    TransactionContextHolder.clearReadOnlyContext();
                    DataSourceContextHolder.clearReadOnlyContext();
                }
            }
        };
    }

}