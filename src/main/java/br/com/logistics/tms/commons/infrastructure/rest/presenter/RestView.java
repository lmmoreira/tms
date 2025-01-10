package br.com.logistics.tms.commons.infrastructure.rest.presenter;

import br.com.logistics.tms.commons.application.presenters.View;
import br.com.logistics.tms.commons.infrastructure.rest.exception.ViewableAdviceException;

public class RestView extends View {

    private final RestViewResponse restViewResponse;
    private final RestViewProblemDetails restViewProblemDetails;

    public RestView(RestViewResponse restViewResponse,
        RestViewProblemDetails restViewProblemDetails) {
        super(restViewResponse != null);
        this.restViewResponse = restViewResponse;
        this.restViewProblemDetails = restViewProblemDetails;
    }

    @Override
    public Object of() {
        if (this.isSuccess()) {
            return this.restViewResponse.getData();
        }

        throw new ViewableAdviceException(this.restViewProblemDetails);
    }
}
