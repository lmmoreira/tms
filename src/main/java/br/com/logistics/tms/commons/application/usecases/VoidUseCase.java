package br.com.logistics.tms.commons.application.usecases;

@FunctionalInterface
public interface VoidUseCase<INPUT> {

    void execute(INPUT input);
}