package br.com.logistics.tms.commons.infrastructure.rest.presenter;

import br.com.logistics.tms.commons.application.presenters.Presenter;
import org.springframework.stereotype.Component;

@Component
public class DefaultRestPresenter<Output> implements Presenter<Output, RestView> {

    @Override
    public RestView present(Output input) {
        return new RestView(new RestViewResponse(input), null);
    }

    @Override
    public RestView present(Throwable error) {
        return new RestView(null, new RestViewProblemDetails(
            "about:blank",
            error.getMessage(),
            500,
            error.getLocalizedMessage(),
            null,
            null));
    }
}
