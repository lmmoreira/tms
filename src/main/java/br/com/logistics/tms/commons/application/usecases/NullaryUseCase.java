package br.com.logistics.tms.commons.application.usecases;

import br.com.logistics.tms.commons.application.presenters.Presenter;
import br.com.logistics.tms.commons.application.presenters.View;

public abstract class NullaryUseCase<OUTPUT> {

    public abstract OUTPUT execute();

    public <T extends View> T execute(Presenter<OUTPUT, T> presenter) {
        try {
            return presenter.present(execute());
        } catch (Throwable t) {
            return presenter.present(t);
        }
    }
}