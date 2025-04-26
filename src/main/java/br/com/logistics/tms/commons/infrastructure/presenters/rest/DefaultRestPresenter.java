package br.com.logistics.tms.commons.infrastructure.presenters.rest;

import br.com.logistics.tms.commons.application.presenters.Presenter;
import br.com.logistics.tms.commons.domain.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DefaultRestPresenter implements Presenter<Object, ResponseEntity<?>> {

    private static Map<Class<?>, HttpStatus> errorStatusMap = Map.of(ValidationException.class, HttpStatus.BAD_REQUEST);

    private final HttpServletRequest request;

    @Autowired
    public DefaultRestPresenter(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public ResponseEntity<?> present(Object input) {
        return present(input, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> present(Object input, HttpStatus successStatus) {
        return ResponseEntity
                .status(successStatus)
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
