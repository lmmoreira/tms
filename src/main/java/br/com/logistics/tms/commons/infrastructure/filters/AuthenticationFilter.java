package br.com.logistics.tms.commons.infrastructure.filters;

import br.com.logistics.tms.commons.infrastructure.security.Company;
import br.com.logistics.tms.commons.infrastructure.security.CompanyContext;
import br.com.logistics.tms.commons.infrastructure.security.User;
import br.com.logistics.tms.commons.infrastructure.security.UserContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@Component
public class AuthenticationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            final HttpServletRequest httpRequest = (HttpServletRequest) request;
            Optional.ofNullable(httpRequest.getHeader("x-client-name"))
                    .ifPresent(value -> {
                        final Company company = new Company(value);
                        CompanyContext.setCurrentCompany(company);
                    });
            Optional.ofNullable(httpRequest.getHeader("x-jwt-details"))
                    .ifPresent(jwt -> {
                        final String[] jwtDetails = jwt.split("\\|");
                        final User user = new User(jwtDetails[0], jwtDetails[1], Set.of(jwtDetails[2].split(",")));
                        UserContext.setCurrentUser(user);
                    });
            chain.doFilter(request, response);
        } finally {
            CompanyContext.clear();
            UserContext.clear();
        }
    }

}