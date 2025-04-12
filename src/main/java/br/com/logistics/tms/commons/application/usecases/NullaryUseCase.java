package br.com.logistics.tms.commons.application.usecases;

@FunctionalInterface
public interface NullaryUseCase<OUTPUT> {

    OUTPUT execute();

}