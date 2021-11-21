package iuh.dhktpm14.cnm.chatappmongo.exceptions;

import iuh.dhktpm14.cnm.chatappmongo.payload.MessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class MyExceptionHandler extends ResponseEntityExceptionHandler {

    @Autowired
    private MessageSource messageSource;

    /**
     * Handle method argument not valid.
     *
     * <p>
     * Phuong thuc nay se duoc goi neu phuong thuc trong controller nhan argument
     * khong hop le
     * </p>
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {
        var bindingResult = ex.getBindingResult();
        if (bindingResult.hasFieldErrors()) {
            List<FieldError> errors = bindingResult.getFieldErrors();
            var fieldError = errors.get(0);
            var response = new MessageResponse(fieldError.getDefaultMessage());
            response.setField(fieldError.getField());
            return ResponseEntity.badRequest().body(response);
        }
        String message = messageSource.getMessage("method_argument_not_valid", null, LocaleContextHolder.getLocale());
        log.error(message);
        return ResponseEntity.badRequest().body(new MessageResponse(message));
    }

    /**
     * Handle property reference exception.
     *
     * <p>
     * Handle PropertyReferenceException
     * </p>
     */
    @ExceptionHandler(PropertyReferenceException.class)
    public final ResponseEntity<Object> handlePropertyReferenceException(PropertyReferenceException ex, WebRequest request) {
        log.error(ex.getMessage());
        return ResponseEntity.badRequest().body(new MessageResponse(ex.getMessage()));
    }

    /**
     * Handle my exception.
     *
     * <p>
     * Hanlde Exception tu dinh nghia
     * </p>
     */
    @ExceptionHandler({ MyException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public final ResponseEntity<Object> handleMyException(Throwable ex, WebRequest request) {
        log.error(ex.getMessage());
        return ResponseEntity.badRequest().body(new MessageResponse(ex.getMessage()));
    }

    /**
     * Handle null pointer exception.
     *
     * @param ex      the ex
     * @param request the request
     * @return the response entity
     */
    @ExceptionHandler(NullPointerException.class)
    public final ResponseEntity<Object> handleNullPointerException(NullPointerException ex, WebRequest request) {
        log.error(ex.getMessage());
        return ResponseEntity.badRequest().body(new MessageResponse("NullPointerException: " + ex.getMessage()));
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                         HttpHeaders headers, HttpStatus status, WebRequest request) {
        String message = messageSource.getMessage("method_not_supported", null, LocaleContextHolder.getLocale());
        log.error(message);
        return ResponseEntity.badRequest().body(new MessageResponse(message));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
                                                                     HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error(ex.getMessage());
        return ResponseEntity.badRequest()
                .body(new MessageResponse("handleHttpMediaTypeNotSupported " + ex.getMessage()));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex,
                                                                      HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error(ex.getMessage());
        return ResponseEntity.badRequest()
                .body(new MessageResponse("handleHttpMediaTypeNotAcceptable " + ex.getMessage()));
    }

    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException ex, HttpHeaders headers,
                                                               HttpStatus status, WebRequest request) {
        log.error(ex.getMessage());
        return ResponseEntity.badRequest().body(new MessageResponse("handleMissingPathVariable " + ex.getMessage()));
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
                                                                          HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error(ex.getMessage());
        return ResponseEntity.badRequest()
                .body(new MessageResponse("handleMissingServletRequestParameter " + ex.getMessage()));
    }

    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex,
                                                                          HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error(ex.getMessage());
        return ResponseEntity.badRequest()
                .body(new MessageResponse("handleServletRequestBindingException " + ex.getMessage()));
    }

    @Override
    protected ResponseEntity<Object> handleConversionNotSupported(ConversionNotSupportedException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error(ex.getMessage());
        return ResponseEntity.badRequest().body(new MessageResponse("handleConversionNotSupported " + ex.getMessage()));
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
                                                        HttpStatus status, WebRequest request) {
        String message = messageSource.getMessage("method_argument_not_valid", null, LocaleContextHolder.getLocale());
        log.error(message);
        return ResponseEntity.badRequest().body(new MessageResponse(message));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {
        String message = messageSource.getMessage("method_argument_not_valid", null, LocaleContextHolder.getLocale());
        log.error(message);
        return ResponseEntity.badRequest().body(new MessageResponse(message));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(HttpMessageNotWritableException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error(ex.getMessage());
        return ResponseEntity.badRequest().body(new MessageResponse("handleHttpMessageNotWritable " + ex.getMessage()));
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(MissingServletRequestPartException ex,
                                                                     HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error(ex.getMessage());
        return ResponseEntity.badRequest()
                .body(new MessageResponse("handleMissingServletRequestPart " + ex.getMessage()));
    }

    @Override
    protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status,
                                                         WebRequest request) {
        log.error(ex.getMessage());
        return ResponseEntity.badRequest().body(new MessageResponse("handleBindException " + ex.getMessage()));
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers,
                                                                   HttpStatus status, WebRequest request) {
        log.error(ex.getMessage());
        return ResponseEntity.badRequest()
                .body(new MessageResponse("handleNoHandlerFoundException " + ex.getMessage()));
    }

    @Override
    protected ResponseEntity<Object> handleAsyncRequestTimeoutException(AsyncRequestTimeoutException ex,
                                                                        HttpHeaders headers, HttpStatus status, WebRequest webRequest) {
        log.error(ex.getMessage());
        return ResponseEntity.badRequest()
                .body(new MessageResponse("handleAsyncRequestTimeoutException " + ex.getMessage()));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatus status, WebRequest request) {
        log.error(ex.getMessage());
        return ResponseEntity.badRequest().body(new MessageResponse("handleExceptionInternal " + ex.getMessage()));
    }

}
