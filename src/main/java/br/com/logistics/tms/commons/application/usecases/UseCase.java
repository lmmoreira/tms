package br.com.logistics.tms.commons.application.usecases;

import br.com.logistics.tms.commons.application.presenters.Presenter;
import br.com.logistics.tms.commons.application.presenters.View;

public abstract class UseCase<INPUT, OUTPUT> {

    public abstract OUTPUT execute(INPUT input);

    public <T extends View> T execute(INPUT input, Presenter<OUTPUT, T> presenter) {
        try {
            return presenter.present(execute(input));
        } catch (Throwable t) {
            return presenter.present(t);
        }
    }
}