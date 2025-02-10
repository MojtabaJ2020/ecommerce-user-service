package com.ecommerce.user_service.model;

import com.ecommerce.user_service.enums.ApiResponseStatusEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ApiErrorResponse extends ApiResponse
{
  @JsonFormat (shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
  private final LocalDateTime timestamp;
  
  private List <ApiError> errors;
  private ApiErrorResponse ()
  {
    super (ApiResponseStatusEnum.ERROR);
    timestamp=LocalDateTime.now();
  }
  public ApiErrorResponse(final List<ApiError> errors)
  {
    this();
    this.errors = errors;
  }
}
