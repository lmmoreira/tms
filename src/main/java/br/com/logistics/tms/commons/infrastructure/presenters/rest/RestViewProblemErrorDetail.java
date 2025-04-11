package br.com.logistics.tms.commons.infrastructure.presenters.rest;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RestViewProblemErrorDetail implements Serializable {

    String field;
    String message;
}
