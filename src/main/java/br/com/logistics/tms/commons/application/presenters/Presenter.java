package br.com.logistics.tms.commons.application.presenters;

public interface Presenter<USECASE_OUTPUT, PRESENTER_OUTPUT> {

    PRESENTER_OUTPUT present(USECASE_OUTPUT input);

    PRESENTER_OUTPUT present(Throwable error);

}