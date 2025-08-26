package br.com.logistics.tms.commons.application.usecases;

import br.com.logistics.tms.commons.application.mapper.Mapper;
import br.com.logistics.tms.commons.application.presenters.Presenter;
import br.com.logistics.tms.commons.application.usecases.exception.UseCaseException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class VoidUseCaseBuilder<INPUT> {
    private final VoidUseCase<INPUT> voidUseCase;
    private final Mapper mapper;
    private final Class<INPUT> inputClass;

    private Object externalInput;
    private Presenter<?, ?> presenter;

    private Consumer<Throwable> exceptionHandler = e -> {
    };

    private final List<UseCaseInterceptor> actionInterceptors = new ArrayList<>();

    public VoidUseCaseBuilder(VoidUseCase<INPUT> voidUseCase) {
        this.voidUseCase = voidUseCase;
        this.mapper = UseCaseMapperProvider.getMapper();
        this.inputClass = resolveInputClass(voidUseCase);

        if (inputClass == null) {
            throw new IllegalStateException("Could not resolve input class for use case");
        }
    }

    public VoidUseCaseBuilder<INPUT> withInput(Object input) {
        this.externalInput = input;
        return this;
    }

    public VoidUseCaseBuilder<INPUT> addInterceptor(final UseCaseInterceptor interceptor) {
        this.actionInterceptors.add(interceptor);
        return this;
    }

    public VoidUseCaseBuilder<INPUT> onException(Consumer<Throwable> handler) {
        this.exceptionHandler = handler;
        return this;
    }

    @SuppressWarnings("unchecked")
    public void execute() {
        Supplier<Void> useCaseExecution = () -> {
            try {
                final INPUT input = !inputClass.isInstance(externalInput)
                        ? mapper.map(externalInput, inputClass)
                        : (INPUT) externalInput;

                voidUseCase.execute(input);

                return null;

            } catch (Exception t) {
                exceptionHandler.accept(t);

                throw new UseCaseException("UseCase Error", t);
            }
        };

        for (int i = actionInterceptors.size() - 1; i >= 0; i--) {
            UseCaseInterceptor interceptor = actionInterceptors.get(i);
            Supplier<Void> previous = useCaseExecution;
            useCaseExecution = () -> interceptor.intercept(previous);
        }

        useCaseExecution.get();
    }

    @SuppressWarnings("unchecked")
    private Class<INPUT> resolveInputClass(VoidUseCase<INPUT> voidUseCase) {
        try {
            for (Type iface : voidUseCase.getClass().getGenericInterfaces()) {
                if (iface instanceof ParameterizedType type && type.getRawType() == VoidUseCase.class) {
                    return (Class<INPUT>) type.getActualTypeArguments()[0];
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}