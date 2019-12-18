package com.cooperative.assembly.error.exception;

public class NotFoundReferenceException extends GenericException {

    private static final long serialVersionUID = 1197999093362401318L;

    private static final String TITLE = "Reference not found";
    private static final String CODE  = "ERR0300";

    public NotFoundReferenceException() {
        super(CODE, TITLE);
    }

    public NotFoundReferenceException(Exception ex) {
        super(CODE, TITLE, ex.getMessage());
    }

    public NotFoundReferenceException(String pointer) {
        this(CODE, TITLE, null, pointer);
    }

    public NotFoundReferenceException(String pointer, String detail) {
        super(CODE, TITLE, detail, pointer);
    }

    public NotFoundReferenceException(String code, String title, String detail) {
        super(code, title, detail);
    }

    public NotFoundReferenceException(String code, String title, String detail, String pointer) {
        super(code, title, detail, pointer);
    }

}
