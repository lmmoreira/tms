package br.com.logistics.tms.commons.application.usecases;

import br.com.logistics.tms.commons.application.mapper.Mapper;
import br.com.logistics.tms.commons.application.presenters.Presenter;
import br.com.logistics.tms.commons.application.usecases.exception.UseCaseException;

import java.util.function.Consumer;
import java.util.function.Function;

public class NullaryUseCaseBuilder<OUTPUT> {
    private final NullaryUseCase<OUTPUT> nullaryUseCase;
    private final Mapper mapper;

    private Class<?> outputClass;
    private Presenter<?, ?> presenter;
    private Function<Object, ?> presenterFunction;

    private Consumer<Throwable> exceptionHandler = e -> {};

    public NullaryUseCaseBuilder(NullaryUseCase<OUTPUT> nullaryUseCase) {
        this.nullaryUseCase = nullaryUseCase;
        this.mapper = UseCaseMapperProvider.getMapper();
    }

    public NullaryUseCaseBuilder<OUTPUT> mapOutputTo(Class<?> outputClass) {
        this.outputClass = outputClass;
        return this;
    }

    public <IN, OUT> NullaryUseCaseBuilder<OUTPUT> presentWith(Presenter<IN, OUT> presenter) {
        this.presenter = presenter;
        return this;
    }

    public <IN, OUT> NullaryUseCaseBuilder<OUTPUT> presentWith(Function<Object, OUT> presenterFunction) {
        this.presenterFunction = presenterFunction;
        return this;
    }

    public NullaryUseCaseBuilder<OUTPUT> onException(Consumer<Throwable> handler) {
        this.exceptionHandler = handler;
        return this;
    }

    @SuppressWarnings("unchecked")
    public <FINAL_OUTPUT> FINAL_OUTPUT execute() {
        try {

            final OUTPUT output = nullaryUseCase.execute();

            final Object finalOutput = outputClass != null
                    ? mapper.map(output, outputClass)
                    : output;

            if (presenter != null) {
                return ((Presenter<Object, FINAL_OUTPUT>) presenter).present(finalOutput);
            }

            if (presenterFunction != null) {
                return (FINAL_OUTPUT) presenterFunction.apply(finalOutput);
            }

            return (FINAL_OUTPUT) finalOutput;
        } catch (Exception t) {
            exceptionHandler.accept(t);

            if (presenter != null) {
                return ((Presenter<Object, FINAL_OUTPUT>) presenter).present(t);
            }

            throw new UseCaseException("UseCase Error", t);
        }
    }

}