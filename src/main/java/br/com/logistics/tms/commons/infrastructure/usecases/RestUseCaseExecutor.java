package br.com.logistics.tms.commons.infrastructure.usecases;

import br.com.logistics.tms.commons.application.usecases.*;
import br.com.logistics.tms.commons.infrastructure.jpa.transaction.Transactional;
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
                .addInterceptor(new UseCaseInterceptor() {
                    @Override
                    public <T> T intercept(Supplier<T> next) {
                        logger.info(useCase.getClass(), "Executing use case: " + useCase.getClass().getSimpleName());
                        return next.get();
                    }
                })
                .addInterceptor(((UseCaseInterceptor) logger))
                .addInterceptor(((UseCaseInterceptor) transactional))
                .onException(e -> logger.error(getClass(), "UseCase failed", e));
    }

    public <INPUT> VoidUseCaseBuilder<INPUT> from(VoidUseCase<INPUT> useCase) {
        return UseCaseExecutor.from(useCase)
                .addInterceptor(new UseCaseInterceptor() {
                    @Override
                    public <T> T intercept(Supplier<T> next) {
                        logger.info(useCase.getClass(), "Executing use case: " + useCase.getClass().getSimpleName());
                        return next.get();
                    }
                })
                .addInterceptor(((UseCaseInterceptor) logger))
                .addInterceptor(((UseCaseInterceptor) transactional))
                .onException(e -> logger.error(getClass(), "UseCase failed", e));
    }
}