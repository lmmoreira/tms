package br.com.logistics.tms.commons.application.presenters;

import org.springframework.http.HttpStatus;

public interface Presenter<USECASE_OUTPUT, PRESENTER_OUTPUT> {

    PRESENTER_OUTPUT present(USECASE_OUTPUT input);

    PRESENTER_OUTPUT present(USECASE_OUTPUT input, HttpStatus successStatus);

    PRESENTER_OUTPUT present(Throwable error);

}