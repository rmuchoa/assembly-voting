package com.cooperative.assembly.voting.error;

import com.cooperative.assembly.voting.response.ResponseJson;
import lombok.extern.java.Log;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
@RestController
@Log
public class ResponseErrorHandler {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = ServletRequestBindingException.class)
    public ResponseJson handleServletRequestException(ServletRequestBindingException srbex) {
        return new ResponseJson(ErrorFactory.errorFromRequestBindingException(srbex));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    public ResponseJson handleMethodArgumentException(MethodArgumentTypeMismatchException matmex) {
        return new ResponseJson(ErrorFactory.errorFromTypeMismatchException(matmex));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseJson handleMethodArgumentNotValidException(MethodArgumentNotValidException manvexc) {
        return new ResponseJson(ErrorFactory.errorFromValidationException(manvexc));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = BindException.class)
    public ResponseJson handleBindException(BindException ex) {
        return new ResponseJson(ErrorFactory.errorFromBindException(ex));
    }

}
