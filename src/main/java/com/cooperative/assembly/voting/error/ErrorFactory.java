package com.cooperative.assembly.voting.error;

import com.cooperative.assembly.voting.error.exception.GenericException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.Arrays;
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
            errorlist.add(new Error(code, title, error.getDefaultMessage(), error.getField(), error.getRejectedValue()));
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
            errorlist.add(new Error(code, title, error.getDefaultMessage(), error.getField(), error.getRejectedValue()));
        });

        return errorlist;
    }

    /**
     * Create error object from a ServletRequestBindingException
     *
     * @param bindExc exception to be converted to list
     * @return list of errors
     */
    public static List<Error> errorFromRequestBindingException(ServletRequestBindingException bindExc) {
        return Arrays.asList(new Error("ERR0100","Incorrect request format", bindExc.getMessage()));
    }

    /**
     * Create error object from a MethodArgumentTypeMismatchException
     *
     * @param typeExc exception to be converted to list
     * @return list of errors
     */
    public static List<Error> errorFromTypeMismatchException(MethodArgumentTypeMismatchException typeExc) {
        return Arrays.asList(new Error("ERR0100","Incorrect request format", null, typeExc.getParameter().getParameterName(), typeExc.getValue()));
    }

    /**
     * Create error object from a GenericException, all custom exceptions should pass here
     *
     * @param generic exception to be converted to list
     * @return list of errors
     */
    public static List<Error> errorFromGenericException(GenericException generic) {
        return Arrays.asList(generic.getError());
    }

    /**
     * Create error object from a Exception, all non-expected exceptions should pass here
     *
     * @param exc exception to be converted to error object
     * @return internal error
     */
    public static List<Error> errorFromException(Exception exc) {
        return Arrays.asList(new Error("ERR9999", "Something went wrong...!!!", exc.getMessage()));
    }

}
