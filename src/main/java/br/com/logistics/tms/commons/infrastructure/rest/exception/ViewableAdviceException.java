package br.com.logistics.tms.commons.infrastructure.rest.exception;

import br.com.logistics.tms.commons.infrastructure.rest.presenter.RestViewProblemDetails;

public class ViewableAdviceException extends RuntimeException {

    private RestViewProblemDetails problemDetails;

    public ViewableAdviceException(RestViewProblemDetails problemDetails) {
        super("", null, true, false);
        this.problemDetails = problemDetails;
    }

    public RestViewProblemDetails getProblemDetails() {
        return problemDetails;
    }

}
