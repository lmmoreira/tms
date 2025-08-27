package br.com.logistics.tms.commons.application.usecases;

import br.com.logistics.tms.commons.application.mapper.Mapper;
import br.com.logistics.tms.commons.application.presenters.Presenter;
import br.com.logistics.tms.commons.application.usecases.exception.UseCaseException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NullaryUseCaseBuilder<OUTPUT> {
    private final NullaryUseCase<OUTPUT> nullaryUseCase;
    private final Mapper mapper;

    private Class<?> outputClass;
    private Presenter<?, ?> presenter;
    private Function<Object, ?> presenterFunctionOnSuccess;
    private Function<Throwable, ?> presenterFunctionOnError;

    private Consumer<Throwable> exceptionHandler = e -> {};

    private final List<UseCaseInterceptor> actionInterceptors = new ArrayList<>();

    public NullaryUseCaseBuilder(NullaryUseCase<OUTPUT> nullaryUseCase) {
        this.nullaryUseCase = nullaryUseCase;
        this.mapper = UseCaseMapperProvider.getMapper();
    }

    public NullaryUseCaseBuilder<OUTPUT> mapOutputTo(Class<?> outputClass) {
        this.outputClass = outputClass;
        return this;
    }

    public NullaryUseCaseBuilder<OUTPUT> presentWith(Presenter<Object, OUTPUT> presenter) {
        this.presenter = presenter;
        return this;
    }

    public NullaryUseCaseBuilder<OUTPUT> presentWith(Function<Object, OUTPUT> presenterFunctionOnSuccess) {
        this.presenterFunctionOnSuccess = presenterFunctionOnSuccess;
        return this;
    }

    public NullaryUseCaseBuilder<OUTPUT> presentWith(Function<Object, OUTPUT> presenterFunctionOnSuccess, Function<Throwable, OUTPUT> presenterFunctionOnError) {
        this.presenterFunctionOnSuccess = presenterFunctionOnSuccess;
        this.presenterFunctionOnError = presenterFunctionOnError;
        return this;
    }

    public NullaryUseCaseBuilder<OUTPUT> addInterceptor(final UseCaseInterceptor interceptor) {
        this.actionInterceptors.add(interceptor);
        return this;
    }

    public NullaryUseCaseBuilder<OUTPUT> onException(Consumer<Throwable> handler) {
        this.exceptionHandler = handler;
        return this;
    }

    @SuppressWarnings("unchecked")
    public <FINAL_OUTPUT> FINAL_OUTPUT execute() {
        Supplier<FINAL_OUTPUT> useCaseExecution = () -> {

            final OUTPUT output = nullaryUseCase.execute();

            final Object finalOutput = outputClass != null
                    ? mapper.map(output, outputClass)
                    : output;

            if (presenter != null) {
                return ((Presenter<Object, FINAL_OUTPUT>) presenter).present(finalOutput);
            }

            if (presenterFunctionOnSuccess != null) {
                return (FINAL_OUTPUT) presenterFunctionOnSuccess.apply(finalOutput);
            }

            return (FINAL_OUTPUT) finalOutput;

        };

        for (int i = actionInterceptors.size() - 1; i >= 0; i--) {
            UseCaseInterceptor interceptor = actionInterceptors.get(i);
            Supplier<FINAL_OUTPUT> previous = useCaseExecution;
            useCaseExecution = () -> interceptor.intercept(previous);
        }

        try {
            return useCaseExecution.get();
        } catch (Exception t) {
            exceptionHandler.accept(t);

            if (presenterFunctionOnError != null) {
                return (FINAL_OUTPUT) presenterFunctionOnError.apply(t);
            }

            if (presenter != null) {
                return ((Presenter<Object, FINAL_OUTPUT>) presenter).present(t);
            }

            throw new UseCaseException("UseCase Error", t);
        }
    }

}