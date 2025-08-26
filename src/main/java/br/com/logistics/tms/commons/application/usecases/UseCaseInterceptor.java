package br.com.logistics.tms.commons.application.usecases;

import java.util.function.Supplier;

@FunctionalInterface
public interface UseCaseInterceptor {
    <T>T intercept(Supplier<T> next);
}