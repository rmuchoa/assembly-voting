package com.cooperative.assembly.voting.response;

import com.cooperative.assembly.voting.error.Error;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to mapping a object to a formatted json response
 *
 * @param <T> body class (data OR error)
 * @param <U> meta class
 */
@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseJson<T, U> {
    private T data;
    private List<Error> errors;
    private U meta;

    public ResponseJson(T body) {
        setBody(body);
    }

    public ResponseJson(T body, U meta) {
        setBody(body);
        this.meta = (meta != null ? meta : null);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void setBody(T body) {
        List values =  new ArrayList<>();

        if(body != null) {
            if (body instanceof List) {
                values.addAll((List) body);
            }

            if (!values.isEmpty() && values.get(0) instanceof Error) {
                errors = values;
            } else {
                data = body;
            }
        } else {
            data = null;
        }
    }

}
