package br.com.logistics.tms.commons.infrastructure.rest.presenter;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RestViewErrorDetail implements Serializable {

    String field;
    String message;
}
