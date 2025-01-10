package br.com.logistics.tms.commons.application.presenters;

import java.io.Serializable;

public abstract class View implements Serializable {

    private final boolean success;

    public View(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public abstract Object of();
}
