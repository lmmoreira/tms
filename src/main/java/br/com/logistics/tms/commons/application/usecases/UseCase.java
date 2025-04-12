package br.com.logistics.tms.commons.application.usecases;

@FunctionalInterface
public interface UseCase<INPUT, OUTPUT> {

    OUTPUT execute(INPUT input);

}