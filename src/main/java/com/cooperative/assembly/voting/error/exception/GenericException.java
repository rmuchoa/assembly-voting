package com.cooperative.assembly.voting.error.exception;

import com.cooperative.assembly.voting.error.Error;

public class GenericException extends RuntimeException{

    private static final long serialVersionUID = 8343122162842739303L;

    private Error error;

    public GenericException(String code, String title) {
        super(title);
        this.error = new Error(code, title);
    }

    public GenericException(String code, String title, String detail) {
        super(title);
        this.error = new Error(code, title, detail);
    }

    public GenericException(String code, String title, String detail, String pointer) {
        super(title);
        this.error = new Error(code, title, detail, pointer);
    }

    public GenericException(String code, String title, String detail, String pointer, Object parameter) {
        super(title);
        this.error = new Error(code, title, detail, pointer, parameter);
    }

    public Error getError() {
        return error;
    }

}
