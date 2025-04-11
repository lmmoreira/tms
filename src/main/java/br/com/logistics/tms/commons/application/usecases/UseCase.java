package br.com.logistics.tms.commons.application.usecases;

import br.com.logistics.tms.commons.application.mapper.Mapper;
import br.com.logistics.tms.commons.application.presenters.Presenter;

public abstract class UseCase<INPUT, OUTPUT> {

    private Mapper mapper;

    protected UseCase() {
    }

    protected UseCase(Mapper mapper) {
        this.mapper = mapper;
    }

    public abstract OUTPUT execute(INPUT input);

    public <P> P execute(INPUT input, Class<P> mapperDestinationType) {
        if (mapper == null) {
            throw new IllegalStateException("Mapper is not set");
        }
        return mapper.map(this.execute(input), mapperDestinationType);
    }

    public <PRESENTER_OUTPUT> PRESENTER_OUTPUT execute(INPUT input, Presenter<OUTPUT, PRESENTER_OUTPUT> presenter) {
        return presenter.present(execute(input));
    }

    public <M, PRESENTER_OUTPUT> PRESENTER_OUTPUT execute(INPUT input, Class<M> mapperDestinationType, Presenter<M, PRESENTER_OUTPUT> presenter) {
        try {

            if (mapper == null) {
                throw new IllegalStateException("Mapper is not set");
            }

            return presenter.present(mapper.map(execute(input), mapperDestinationType));
        } catch (Throwable t) {
            return presenter.present(t);
        }
    }
}