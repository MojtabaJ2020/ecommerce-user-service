package com.ecommerce.user_service.aop;

import com.ecommerce.user_service.constant.DefaultMessageSource;
import com.ecommerce.user_service.enums.ApiErrorCodeEnum;
import com.ecommerce.user_service.exception.DuplicateUserException;
import com.ecommerce.user_service.exception.EncryptionException;
import com.ecommerce.user_service.exception.InvalidRefreshTokenException;
import com.ecommerce.user_service.exception.NotSupportedException;
import com.ecommerce.user_service.exception.UserInactiveException;
import com.ecommerce.user_service.exception.UserNotFoundException;
import com.ecommerce.user_service.exception.UserPictureIOException;
import com.ecommerce.user_service.model.ApiError;
import com.ecommerce.user_service.model.ApiErrorResponse;
import com.ecommerce.user_service.model.ApiSubError;
import com.ecommerce.user_service.service.LogSanitizerService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j @RestControllerAdvice public class GlobalExceptionHandler
{
  private static final String DEFAULT_VALIDATION_EXCEPTION_MESSAGE = "Invalid data!";
  private final MessageSource messageSource;
  private final LogSanitizerService sanitizerService;
  @Value ("${debug-mode:false}") private boolean isDebugMode;
  
  @Autowired public GlobalExceptionHandler (MessageSource messageSource, LogSanitizerService sanitizerService)
  {
    this.messageSource = messageSource;
    this.sanitizerService = sanitizerService;
  }
  
  @ExceptionHandler (AuthenticationException.class)
  public ResponseEntity <?> handleAuthenticationException (AuthenticationException ex, HttpServletRequest request)
  {
    String message = messageSource.getMessage ("user.login.failed", null, DefaultMessageSource.ERROR_MESSAGE, request.getLocale ());
    
    addLog (ex.getMessage (), request, ex);
    
    ApiError apiError = ApiError.builder ()
                                .message (message)
                                .errorCode (ApiErrorCodeEnum.UNAUTHORIZED.getCode ())
                                .help (null)
                                .debugMessage (isDebugMode ? ex.getLocalizedMessage () : null)
                                .subErrors (null)
                                .build ();
    
    return ResponseEntity.status (HttpStatus.UNAUTHORIZED).body (new ApiErrorResponse (List.of (apiError)));
  }
  
  @ExceptionHandler (MethodArgumentNotValidException.class)
  public ResponseEntity <?> handleValidationExceptions (MethodArgumentNotValidException ex, HttpServletRequest request)
  {
    Map <String, String> errors = ex.getBindingResult ()
                                    .getAllErrors ()
                                    .stream ()
                                    .collect (Collectors.toMap (error -> ((FieldError) error).getField (),
                                                                error -> error.getDefaultMessage () != null ? error.getDefaultMessage () :
                                                                         DEFAULT_VALIDATION_EXCEPTION_MESSAGE,
                                                                (a, b) -> b));
    
    List <ApiSubError> apiSubErrors = new ArrayList <> ();
    errors.forEach ((filed, errorMessage) ->
                    {
                      String validationMessage = messageSource.getMessage (errorMessage, null, DefaultMessageSource.INVALID_MESSAGE, request.getLocale ());
                      apiSubErrors.add (ApiSubError.builder().object ("user").field (filed).message (validationMessage).build ());
                    });
    
    String message = messageSource.getMessage ("invalid.data", null, DefaultMessageSource.INFO_MESSAGE, request.getLocale ());
    
    addLog (ex.getMessage (), request, ex);
    
    ApiError apiError = ApiError.builder ()
                                .message (message)
                                .errorCode (ApiErrorCodeEnum.INVALID_INPUT.getCode ())
                                .help (null)
                                .debugMessage (isDebugMode ? ex.getLocalizedMessage () : null)
                                .subErrors (apiSubErrors)
                                .build ();
    
    return ResponseEntity.status (HttpStatus.BAD_REQUEST).body (new ApiErrorResponse (List.of (apiError)));
  }
  
  @ExceptionHandler (UserPictureIOException.class)
  public ResponseEntity <?> handleValidationExceptions (UserPictureIOException ex, HttpServletRequest request)
  {
    String message = messageSource.getMessage ("user.picture.io.error", null, DefaultMessageSource.ERROR_MESSAGE, request.getLocale ());

    addLog (ex.getMessage (), request, ex);
    
    ApiError apiError = ApiError.builder ()
                                .message (message)
                                .errorCode (ApiErrorCodeEnum.IO_ERROR.getCode ())
                                .help (null)
                                .debugMessage (isDebugMode ? ex.getLocalizedMessage () : null)
                                .subErrors (null)
                                .build ();
    
    return ResponseEntity.status (HttpStatus.INTERNAL_SERVER_ERROR).body (new ApiErrorResponse (List.of (apiError)));
  }
  
  @ExceptionHandler (HttpMediaTypeNotAcceptableException.class)
  public ResponseEntity <?> handleMediaTypeError (HttpMediaTypeNotAcceptableException ex, HttpServletRequest request)
  {
    String supportedTypes = ex.getSupportedMediaTypes ().stream ().map (MediaType::toString).toList ().toString ();
    String message = messageSource.getMessage ("unsupported.response", null, DefaultMessageSource.ERROR_MESSAGE, request.getLocale ());
    
    addLog (ex.getMessage (), request, ex);
    
    ApiError apiError = ApiError.builder ()
                                .message (message)
                                .errorCode (ApiErrorCodeEnum.NOT_SUPPORTED.getCode ())
                                .help ("Supported types are:"+supportedTypes)
                                .debugMessage (isDebugMode ? ex.getLocalizedMessage () : null)
                                .subErrors (null)
                                .build ();
    
    return ResponseEntity.status (HttpStatus.NOT_ACCEPTABLE).body (new ApiErrorResponse (List.of (apiError)));
  }
  
  @ExceptionHandler (JwtException.class) public ResponseEntity <?> handleJwtException (JwtException ex, HttpServletRequest request)
  {
    String message = messageSource.getMessage ("user.invalid.jwt.token", null, DefaultMessageSource.ERROR_MESSAGE, request.getLocale ());
    
    addLog (ex.getMessage (), request, ex);
    
    ApiError apiError = ApiError.builder ()
                                .message (message)
                                .errorCode (ApiErrorCodeEnum.UNAUTHORIZED.getCode ())
                                .help (null)
                                .debugMessage (isDebugMode ? ex.getLocalizedMessage () : null)
                                .subErrors (null)
                                .build ();
    
    return ResponseEntity.status (HttpStatus.UNAUTHORIZED).body (new ApiErrorResponse (List.of (apiError)));
  }
  
  @ExceptionHandler (UserInactiveException.class)
  public ResponseEntity <?> handleUserInactiveException (UserInactiveException ex, HttpServletRequest request)
  {
    String message = messageSource.getMessage ("user.inactive", null, DefaultMessageSource.ERROR_MESSAGE, request.getLocale ());
    
    addLog (ex.getMessage (), request, ex);
    
    ApiError apiError = ApiError.builder ()
                                .message (message)
                                .errorCode (ApiErrorCodeEnum.USER_INACTIVE.getCode ())
                                .help (null)
                                .debugMessage (isDebugMode ? ex.getLocalizedMessage () : null)
                                .subErrors (null)
                                .build ();
    
    return ResponseEntity.status (HttpStatus.UNAUTHORIZED).body (new ApiErrorResponse (List.of (apiError)));
  }
  
  @ExceptionHandler (UserNotFoundException.class)
  public ResponseEntity <?> handleUserNotFoundException (UserNotFoundException ex, HttpServletRequest request)
  {
    String message = messageSource.getMessage ("user.not.found", null, DefaultMessageSource.ERROR_MESSAGE, request.getLocale ());
    
    addLog (ex.getMessage (), request, ex);
    
    ApiError apiError = ApiError.builder ()
                                .message (message)
                                .errorCode (ApiErrorCodeEnum.USER_NOT_FOUND.getCode ())
                                .help (null)
                                .debugMessage (isDebugMode ? ex.getLocalizedMessage () : null)
                                .subErrors (null)
                                .build ();
    
    return ResponseEntity.status (HttpStatus.NOT_FOUND).body (new ApiErrorResponse (List.of (apiError)));
  }
  
  @ExceptionHandler (DuplicateUserException.class)
  public ResponseEntity <?> handleDuplicateUserException (DuplicateUserException ex, HttpServletRequest request)
  {
    String message = messageSource.getMessage ("user.duplicate", null, DefaultMessageSource.ERROR_MESSAGE, request.getLocale ());
    
    addLog (ex.getMessage (), request, ex);
    
    ApiError apiError = ApiError.builder ()
                                .message (message)
                                .errorCode (ApiErrorCodeEnum.USER_DUPLICATE.getCode ())
                                .help ("Use another username or email")
                                .debugMessage (isDebugMode ? ex.getLocalizedMessage () : null)
                                .subErrors (null)
                                .build ();
    
    return ResponseEntity.status (HttpStatus.CONFLICT).body (new ApiErrorResponse (List.of (apiError)));
    
  }
  
  @ExceptionHandler (InvalidRefreshTokenException.class)
  public ResponseEntity <?> handleInvalidRefreshTokenException (InvalidRefreshTokenException ex, HttpServletRequest request)
  {
    String message = messageSource.getMessage ("user.invalid.refresh.token", null, DefaultMessageSource.INVALID_MESSAGE, request.getLocale ());
    
    addLog (ex.getMessage (), request, ex);
    
    ApiError apiError = ApiError.builder ()
                                .message (message)
                                .errorCode (ApiErrorCodeEnum.INVALID_REFRESH_TOKEN.getCode ())
                                .help ("Try to re-login")
                                .debugMessage (isDebugMode ? ex.getLocalizedMessage () : null)
                                .subErrors (null)
                                .build ();
    
    return ResponseEntity.status (HttpStatus.UNAUTHORIZED).body (new ApiErrorResponse (List.of (apiError)));
  }
  
  @ExceptionHandler (EncryptionException.class) public ResponseEntity <?> handleEncryptionException (EncryptionException ex, HttpServletRequest request)
  {
    String message = messageSource.getMessage ("error", null, DefaultMessageSource.ERROR_MESSAGE, request.getLocale ());
    
    addLog (ex.getMessage (), request, ex);
    
    ApiError apiError = ApiError.builder ()
                                .message (message)
                                .errorCode (ApiErrorCodeEnum.ENCRYPT_DECRYPT.getCode ())
                                .help (null)
                                .debugMessage (isDebugMode ? ex.getLocalizedMessage () : null)
                                .subErrors (null)
                                .build ();
    
    return ResponseEntity.status (HttpStatus.INTERNAL_SERVER_ERROR).body (new ApiErrorResponse (List.of (apiError)));
    
  }
  
  @ExceptionHandler (NotSupportedException.class)
  public ResponseEntity <?> handleNotSupportedException (NotSupportedException ex, HttpServletRequest request)
  {
    String message = messageSource.getMessage ("error", null, DefaultMessageSource.ERROR_MESSAGE, request.getLocale ());
    
    addLog (ex.getMessage (), request, ex);
    
    ApiError apiError = ApiError.builder ()
                                .message (message)
                                .errorCode (ApiErrorCodeEnum.NOT_SUPPORTED.getCode ())
                                .help (null)
                                .debugMessage (isDebugMode ? ex.getLocalizedMessage () : null)
                                .subErrors (null)
                                .build ();
    
    return ResponseEntity.status (HttpStatus.INTERNAL_SERVER_ERROR).body (new ApiErrorResponse (List.of (apiError)));
  }
  
  @ExceptionHandler (Exception.class) public ResponseEntity <?> handleException (Exception ex, HttpServletRequest request)
  {
    String message = messageSource.getMessage ("error", null, DefaultMessageSource.ERROR_MESSAGE, request.getLocale ());
    
    addLog ("Unhandled exception", request, ex);
    
    ApiError apiError = ApiError.builder ()
                                .message (message)
                                .errorCode (ApiErrorCodeEnum.INTERNAL_ERROR.getCode ())
                                .help (null)
                                .debugMessage (isDebugMode ? ex.getLocalizedMessage () : null)
                                .subErrors (null)
                                .build ();
    
    return ResponseEntity.status (HttpStatus.INTERNAL_SERVER_ERROR).body (new ApiErrorResponse (List.of (apiError)));
  }
  
  @Async ("loggingTaskExecutor") private void addLog (String message, HttpServletRequest request, Exception ex)
  {
    if (isDebugMode)
    {
      log.error (sanitizerService.error (message, request), ex);
    }
    else
    {
      log.error (sanitizerService.error (message, request));
    }
  }
}
