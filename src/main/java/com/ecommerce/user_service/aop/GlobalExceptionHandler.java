package com.ecommerce.user_service.aop;

import com.ecommerce.user_service.exception.UserPictureIOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice public class GlobalExceptionHandler
{
  private static final String DEFAULT_VALIDATION_EXCEPTION_MESSAGE = "Invalid data!";
  
  @ExceptionHandler (MethodArgumentNotValidException.class)
  public ResponseEntity <Map <String, String>> handleValidationExceptions (MethodArgumentNotValidException ex)
  {
    Map <String, String> errors = ex.getBindingResult ()
                                    .getAllErrors ()
                                    .stream ()
                                    .collect (Collectors.toMap (error -> ((FieldError) error).getField (),
                                                                error -> error.getDefaultMessage () != null ? error.getDefaultMessage () :
                                                                         DEFAULT_VALIDATION_EXCEPTION_MESSAGE,
                                                                (a, b) -> b));
    return new ResponseEntity <> (errors, HttpStatus.BAD_REQUEST);
  }
  
  @ExceptionHandler (UserPictureIOException.class)
  public ResponseEntity <?> handleValidationExceptions (UserPictureIOException ex)
  {
    return new ResponseEntity <> (ex.getMessage (), HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
