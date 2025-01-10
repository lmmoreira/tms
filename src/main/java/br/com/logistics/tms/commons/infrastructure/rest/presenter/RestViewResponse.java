package br.com.logistics.tms.commons.infrastructure.rest.presenter;

import br.com.logistics.tms.commons.application.presenters.View;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RestViewResponse implements Serializable {
    private Object data;
}