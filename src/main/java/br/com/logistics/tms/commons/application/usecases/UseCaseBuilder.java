package br.com.logistics.tms.commons.application.usecases;

import br.com.logistics.tms.commons.application.mapper.Mapper;
import br.com.logistics.tms.commons.application.presenters.Presenter;
import br.com.logistics.tms.commons.application.usecases.exception.UseCaseException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class UseCaseBuilder<INPUT, OUTPUT> {
    private final UseCase<INPUT, OUTPUT> useCase;
    private final Mapper mapper;
    private final Class<INPUT> inputClass;

    private Object externalInput;
    private Class<?> outputClass;
    private Presenter<?, ?> presenter;
    private Function<Object, ?> presenterFunctionOnSuccess;
    private Function<Throwable, ?> presenterFunctionOnError;
    private Consumer<Throwable> exceptionHandler = e -> {
    };

    private final List<UseCaseInterceptor> actionInterceptors = new ArrayList<>();

    public UseCaseBuilder(UseCase<INPUT, OUTPUT> useCase) {
        this.useCase = useCase;
        this.mapper = UseCaseMapperProvider.getMapper();
        this.inputClass = resolveInputClass(useCase);

        if (inputClass == null) {
            throw new IllegalStateException("Could not resolve input class for use case");
        }
    }

    public UseCaseBuilder<INPUT, OUTPUT> withInput(Object input) {
        this.externalInput = input;
        return this;
    }

    public UseCaseBuilder<INPUT, OUTPUT> mapOutputTo(Class<?> outputClass) {
        this.outputClass = outputClass;
        return this;
    }

    public <IN, OUT> UseCaseBuilder<INPUT, OUTPUT> presentWith(Presenter<IN, OUT> presenter) {
        this.presenter = presenter;
        return this;
    }

    public <IN, OUT> UseCaseBuilder<INPUT, OUTPUT> presentWith(Function<Object, OUT> presenterFunctionOnSuccess) {
        this.presenterFunctionOnSuccess = presenterFunctionOnSuccess;
        return this;
    }

    public <IN, OUT> UseCaseBuilder<INPUT, OUTPUT> presentWith(Function<Object, OUT> presenterFunctionOnSuccess, Function<Throwable, OUT> presenterFunctionOnError) {
        this.presenterFunctionOnSuccess = presenterFunctionOnSuccess;
        this.presenterFunctionOnError = presenterFunctionOnError;
        return this;
    }

    public UseCaseBuilder<INPUT, OUTPUT> addInterceptor(final UseCaseInterceptor interceptor) {
        this.actionInterceptors.add(interceptor);
        return this;
    }

    public UseCaseBuilder<INPUT, OUTPUT> onException(Consumer<Throwable> handler) {
        this.exceptionHandler = handler;
        return this;
    }

    @SuppressWarnings("unchecked")
    public <FINAL_OUTPUT> FINAL_OUTPUT execute() {
        Supplier<FINAL_OUTPUT> useCaseExecution = () -> {
            final INPUT input = !inputClass.isInstance(externalInput)
                    ? mapper.map(externalInput, inputClass)
                    : (INPUT) externalInput;

            final OUTPUT output = useCase.execute(input);

            final Object finalOutput = outputClass != null
                    ? mapper.map(output, outputClass)
                    : output;

            if (presenterFunctionOnSuccess != null) {
                return (FINAL_OUTPUT) presenterFunctionOnSuccess.apply(finalOutput);
            }

            if (presenter != null) {
                return ((Presenter<Object, FINAL_OUTPUT>) presenter).present(finalOutput);
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

    @SuppressWarnings("unchecked")
    private Class<INPUT> resolveInputClass(UseCase<INPUT, OUTPUT> useCase) {
        try {
            for (Type iface : useCase.getClass().getGenericInterfaces()) {
                if (iface instanceof ParameterizedType type && type.getRawType() == UseCase.class) {
                    return (Class<INPUT>) type.getActualTypeArguments()[0];
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}