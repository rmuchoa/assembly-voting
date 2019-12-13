package com.cooperative.assembly.voting.error;

import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;

public class ErrorFactory {

    /**
     * Create error list from a MethodArgumentNotValidException
     *
     * @param valException exception to be converted to list
     * @return list of errors
     */
    public static List<Error> errorFromValidationException(MethodArgumentNotValidException valException) {
        String code = "ERR0100";
        String title = "Incorrect request format";
        List<Error> errorlist = new ArrayList<Error>();
        valException.getBindingResult().getFieldErrors().forEach( error -> {
            errorlist.add(new Error(code, title, getDefaultMessage(error), error.getField(), error.getRejectedValue()));
        });

        return errorlist;
    }

    /**
     * Create error list from a BindException
     *
     * @param exception to be converted to list
     * @return list of errors
     */
    public static List<Error> errorFromBindException(BindException exception) {
        String code = "ERR0100";
        String title = "Incorrect request format";
        List<Error> errorlist = new ArrayList<Error>();
        exception.getBindingResult().getFieldErrors().forEach( error -> {
            errorlist.add(new Error(code, title, getDefaultMessage(error), error.getField(), error.getRejectedValue()));
        });

        return errorlist;
    }

    /**
     * Create error object from a ServletRequestBindingException
     *
     * @param bindExc exception to be converted to list
     * @return list of errors
     */
    public static Error errorFromRequestBindingException(ServletRequestBindingException bindExc) {
        return new Error("ERR0100","Incorrect request format", bindExc.getMessage());
    }

    /**
     * Create error object from a MethodArgumentTypeMismatchException
     *
     * @param typeExc exception to be converted to list
     * @return list of errors
     */
    public static Error errorFromTypeMismatchException(MethodArgumentTypeMismatchException typeExc) {
        return new Error("ERR0100","Incorrect request format", null, typeExc.getParameter().getParameterName(), typeExc.getValue());
    }

    /**
     * Create error object from a Exception, all non-expected exceptions should pass here
     *
     * @param exc exception to be converted to error object
     * @return internal error
     */
    public static Error errorFromException(Exception exc) {
        return new Error("ERR9999", "Something went wrong...!!!", exc.getMessage());
    }

    private static String getDefaultMessage(FieldError error) {
        return error.getDefaultMessage().replaceAll("[\\[\\](){}]","");
    }

}
