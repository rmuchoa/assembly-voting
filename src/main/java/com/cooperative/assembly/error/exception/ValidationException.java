package com.cooperative.assembly.error.exception;

public class ValidationException extends GenericException {

	private static final long serialVersionUID = -6723793316803730300L;

	private static final String TITLE = "Invalid parameter";
    private static final String CODE = "ERR0400";

    public ValidationException() {
        super(CODE, TITLE);
    }

    public ValidationException(String detail) {
        super(CODE, TITLE, detail);
    }

    public ValidationException(String detail, String pointer, Object value) {
        super(CODE, TITLE, detail, pointer, (value != null ? value.toString(): null));
    }

}
