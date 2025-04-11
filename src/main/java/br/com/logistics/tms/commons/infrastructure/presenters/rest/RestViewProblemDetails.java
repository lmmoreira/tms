package br.com.logistics.tms.commons.infrastructure.presenters.rest;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class RestViewProblemDetails implements Serializable {
    String type;
    String title;
    int status;
    String detail;
    String instance;
    List<RestViewProblemErrorDetail> errors;

    public RestViewProblemDetails withInstance(String instance) {
        return new RestViewProblemDetails(this.type, this.title, this.status, this.detail, instance,
            this.errors);
    }

}