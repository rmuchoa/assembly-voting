package com.cooperative.assembly.voting.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Error {

    private String code;
    private String detail;
    private String title;
    private Source source;

    public Error(String code, String title) {
        this.code = code;
        this.title = title;
    }

    public Error(String code, String title, String detail) {
        this.code = code;
        this.detail = detail;
        this.title = title;
    }

    public Error(String code, String title, String detail, String pointer) {
        this.code = code;
        this.detail = detail;
        this.title = title;
        this.source = new Source(pointer);
    }

    public Error(String code, String title, String detail, String pointer, Object parameter) {
        this.code = code;
        this.detail = detail;
        this.title = title;
        this.source = new Source(pointer, (parameter != null ? parameter.toString() : null));
    }

}
