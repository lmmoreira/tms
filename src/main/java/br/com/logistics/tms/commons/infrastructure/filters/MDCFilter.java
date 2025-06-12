package br.com.logistics.tms.commons.infrastructure.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class MDCFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        try {
            final HttpServletRequest httpRequest = (HttpServletRequest) request;

            Optional.ofNullable(httpRequest.getHeader("x-request-id"))
                .ifPresent(value -> MDC.put("request_id", value));
            Optional.ofNullable(httpRequest.getHeader("x-correlation-id"))
                    .ifPresent(value -> MDC.put("correlation_id", value));
            Optional.ofNullable(httpRequest.getHeader("x-module"))
                .ifPresent(value -> MDC.put("module", value));
            Optional.ofNullable(httpRequest.getHeader("x-client-name"))
                .ifPresent(value -> MDC.put("client_name", value));
            Optional.ofNullable(httpRequest.getHeader("x-jwt-details"))
                    .ifPresent(jwt -> {
                        final String[] jwtDetails = jwt.split("\\|");
                        MDC.put("user_name", jwtDetails[0]);
                        MDC.put("user_email", jwtDetails[1]);
                    });

            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

}