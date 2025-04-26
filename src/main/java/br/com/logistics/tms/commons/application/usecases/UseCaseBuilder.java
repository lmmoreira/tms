package br.com.logistics.tms.commons.application.usecases;

import br.com.logistics.tms.commons.application.mapper.Mapper;
import br.com.logistics.tms.commons.application.presenters.Presenter;
import br.com.logistics.tms.commons.application.usecases.exception.UseCaseException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Function;

public class UseCaseBuilder<INPUT, OUTPUT> {
    private final UseCase<INPUT, OUTPUT> useCase;
    private final Mapper mapper;
    private final Class<INPUT> inputClass;

    private Object externalInput;
    private Class<?> outputClass;
    private Presenter<?, ?> presenter;
    private Function<Object, ?> presenterFunction;

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

    public <IN, OUT> UseCaseBuilder<INPUT, OUTPUT> presentWith(Presenter<IN, OUT> presenter, Function<Object, OUT> presenterFunction) {
        this.presenter = presenter;
        this.presenterFunction = presenterFunction;
        return this;
    }

    @SuppressWarnings("unchecked")
    public <FINAL_OUTPUT> FINAL_OUTPUT execute() {
        try {
            final INPUT input = !inputClass.isInstance(externalInput)
                    ? mapper.map(externalInput, inputClass)
                    : (INPUT) externalInput;

            final OUTPUT output = useCase.execute(input);

            final Object finalOutput = outputClass != null
                    ? mapper.map(output, outputClass)
                    : output;

            if (presenter != null) {

                if (presenterFunction != null) {
                    return (FINAL_OUTPUT) presenterFunction.apply(finalOutput);
                } else {
                    return ((Presenter<Object, FINAL_OUTPUT>) presenter).present(finalOutput);
                }

            }

            return (FINAL_OUTPUT) finalOutput;
        } catch (Exception t) {
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