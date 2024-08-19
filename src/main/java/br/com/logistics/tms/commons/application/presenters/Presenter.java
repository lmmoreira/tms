package br.com.logistics.tms.commons.application.presenters;

public interface Presenter<IN, OUT> {

    OUT present(IN input);

    OUT present(Throwable error);
}