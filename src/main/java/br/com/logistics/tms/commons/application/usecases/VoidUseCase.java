package br.com.logistics.tms.commons.application.usecases;

public abstract class VoidUseCase<INPUT> {

    public abstract void execute(INPUT input);
}