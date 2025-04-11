package br.com.logistics.tms.commons.infrastructure.presenters.rest;

import br.com.logistics.tms.commons.application.presenters.Presenter;
import br.com.logistics.tms.commons.domain.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Scope("prototype")
public class DefaultRestPresenter implements Presenter<Object, ResponseEntity<?>> {

    private final HttpServletRequest request;
    private HttpStatus success = HttpStatus.OK;

    private static Map<Class<?>, HttpStatus> errorStatusMap = Map.of(ValidationException.class, HttpStatus.BAD_REQUEST);

    @Autowired
    public DefaultRestPresenter(HttpServletRequest request) {
        this.request = request;
    }

    @SuppressWarnings("unchecked")
    public <T> Presenter<T, ResponseEntity<?>> typed() {
        return (Presenter<T, ResponseEntity<?>>) this;
    }

    public DefaultRestPresenter withResponseStatus(HttpStatus status) {
        this.success = status;
        return this;
    }

    @Override
    public ResponseEntity<?> present(Object input) {
        return ResponseEntity
                .status(success)
                .body(input);
    }

    @Override
    public ResponseEntity<RestViewProblemDetails> present(Throwable error) {
        HttpStatus errorStatus = errorStatusMap.getOrDefault(error.getClass(), HttpStatus.INTERNAL_SERVER_ERROR);

        return ResponseEntity
                .status(errorStatus)
                .body(new RestViewProblemDetails(
                        "about:blank",
                        error.getMessage(),
                        errorStatus.value(),
                        error.getLocalizedMessage(),
                        request.getRequestURI(),
                        null));
    }
}
