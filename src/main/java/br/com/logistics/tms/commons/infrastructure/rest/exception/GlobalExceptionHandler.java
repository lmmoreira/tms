package br.com.logistics.tms.commons.infrastructure.rest.exception;

import br.com.logistics.tms.commons.infrastructure.rest.presenter.RestViewProblemDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ViewableAdviceException.class)
    public ResponseEntity<RestViewProblemDetails> handleDomainExceptions(ViewableAdviceException ex,
        HttpServletRequest request) {
        return ResponseEntity
            .status(ex.getProblemDetails().getStatus())
            .body(ex.getProblemDetails().withInstance(request.getRequestURI()));
    }

}
