package br.com.logistics.tms.commons.application.usecases;

import br.com.logistics.tms.commons.application.mapper.Mapper;
import br.com.logistics.tms.commons.application.presenters.Presenter;
import br.com.logistics.tms.commons.application.usecases.exception.UseCaseException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class VoidUseCaseBuilder<INPUT> {
    private final VoidUseCase<INPUT> voidUseCase;
    private final Mapper mapper;
    private final Class<INPUT> inputClass;

    private Object externalInput;
    private Presenter<?, ?> presenter;

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

    @SuppressWarnings("unchecked")
    public void execute() {
        try {
            final INPUT input = !inputClass.isInstance(externalInput)
                    ? mapper.map(externalInput, inputClass)
                    : (INPUT) externalInput;

            voidUseCase.execute(input);

        } catch (Exception t) {
            throw new UseCaseException("UseCase Error", t);
        }
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