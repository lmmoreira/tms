package br.com.logistics.tms.commons.infrastructure.usecases;

import br.com.logistics.tms.commons.application.usecases.UseCase;
import br.com.logistics.tms.commons.application.usecases.UseCaseBuilder;
import br.com.logistics.tms.commons.application.usecases.UseCaseExecutor;
import br.com.logistics.tms.commons.infrastructure.presenters.rest.DefaultRestPresenter;
import br.com.logistics.tms.commons.infrastructure.telemetry.Logable;
import org.springframework.stereotype.Component;

@Component
public class RestUseCaseExecutor {

    private final DefaultRestPresenter defaultRestPresenter;
    private final Logable logger;

    public RestUseCaseExecutor(DefaultRestPresenter defaultRestPresenter,
                               Logable logger) {
        this.defaultRestPresenter = defaultRestPresenter;
        this.logger = logger;
    }

    public <INPUT, OUTPUT> UseCaseBuilder<INPUT, OUTPUT> from(UseCase<INPUT, OUTPUT> useCase) {
        return UseCaseExecutor.from(useCase)
                .presentWith(defaultRestPresenter)
                .onException(e -> logger.error(getClass(), "UseCase failed", e));
    }
}