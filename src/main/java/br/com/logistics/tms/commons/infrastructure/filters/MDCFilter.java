package br.com.logistics.tms.commons.infrastructure.filters;

import com.auth0.jwt.JWT;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class MDCFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        try {
            final HttpServletRequest httpRequest = (HttpServletRequest) request;

            Optional.ofNullable(httpRequest.getHeader("x-request-id"))
                .ifPresent(value -> MDC.put("request_id", value));
            Optional.ofNullable(httpRequest.getHeader("x-module"))
                .ifPresent(value -> MDC.put("module", value));
            Optional.ofNullable(httpRequest.getHeader("x-client-name"))
                .ifPresent(value -> MDC.put("client_name", value));
            Optional.ofNullable(httpRequest.getHeader("authorization"))
                .map(token -> token.replace("Bearer ", ""))
                .map(JWT::decode)
                .ifPresent(jwt -> {
                    MDC.put("user_name", jwt.getClaim("given_name").asString());
                    MDC.put("user_email", jwt.getClaim("email").asString());
                });

            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

}