package br.com.logistics.tms.commons.application.usecases;

import br.com.logistics.tms.commons.application.presenters.Presenter;

public abstract class NullaryUseCase<OUTPUT> {

    public abstract OUTPUT execute();

    public Object execute(Presenter presenter) {
        try {
            return presenter.present(execute());
        } catch (Throwable t) {
            return presenter.present(t);
        }
    }
}