package br.com.logistics.tms.commons.application.usecases;

public class UseCaseExecutor {

    public static <INPUT, OUTPUT> UseCaseBuilder<INPUT, OUTPUT> from(UseCase<INPUT, OUTPUT> useCase) {
        return new UseCaseBuilder<>(useCase);
    }

    public static <INPUT> VoidUseCaseBuilder<INPUT> from(VoidUseCase<INPUT> useCase) {
        return new VoidUseCaseBuilder<>(useCase);
    }

    public static <OUTPUT> NullaryUseCaseBuilder<OUTPUT> from(NullaryUseCase<OUTPUT> useCase) {
        return new NullaryUseCaseBuilder<>(useCase);
    }

}
