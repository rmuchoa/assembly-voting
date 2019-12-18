package com.cooperative.assembly.error;

import com.cooperative.assembly.error.exception.GenericException;
import com.cooperative.assembly.error.exception.NotFoundReferenceException;
import com.cooperative.assembly.response.ResponseJson;
import lombok.extern.log4j.Log4j2;
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
@Log4j2
public class ResponseErrorHandler {

    /**
     * Returns 400 BadRequest status for handled Spring web ServletRequestBindingException.class
     * @param ex
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = ServletRequestBindingException.class)
    public ResponseJson handleServletRequestException(ServletRequestBindingException ex) {
        log.error(ex.getMessage(), ex);
        return new ResponseJson(ErrorFactory.errorFromRequestBindingException(ex));
    }

    /**
     * Returns 400 BadRequest status for handled Spring web MethodArgumentTypeMismatchException.class
     * @param ex
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    public ResponseJson handleMethodArgumentException(MethodArgumentTypeMismatchException ex) {
        log.error(ex.getMessage(), ex);
        return new ResponseJson(ErrorFactory.errorFromTypeMismatchException(ex));
    }

    /**
     * Returns 400 BadRequest status for handled Spring web MethodArgumentNotValidException.class
     * @param ex
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseJson handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error(ex.getMessage(), ex);
        return new ResponseJson(ErrorFactory.errorFromValidationException(ex));
    }

    /**
     * Return 400 BadRequest status for handled Spring validation BindException.class
     * @param ex
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = BindException.class)
    public ResponseJson handleBindException(BindException ex) {
        log.error(ex.getMessage(), ex);
        return new ResponseJson(ErrorFactory.errorFromBindException(ex));
    }

    /**
     * Returns 500 status for handled NotFoundReferenceException.class
     * @param ex
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = NotFoundReferenceException.class)
    public ResponseJson handleInternalErrorException(NotFoundReferenceException ex) {
        log.error(ex.getMessage(), ex);
        return new ResponseJson(ErrorFactory.errorFromGenericException(ex));
    }

    /**
     * Will catchs all exceptions from generic without a especific handler and returns 500 InternalServerError status
     * @param ex
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = GenericException.class)
    public ResponseJson handleGenericException(GenericException ex) {
        log.error(ex.getMessage(), ex);
        return new ResponseJson(ErrorFactory.errorFromGenericException(ex));
    }

    /**
     * Will cathes all un-handled exceptions and returns 500 InternalServerError status
     * @param ex
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = Exception.class)
    public ResponseJson handleException(Exception ex) {
        log.error(ex.getMessage(), ex);
        return new ResponseJson(ErrorFactory.errorFromException(ex));
    }

}
