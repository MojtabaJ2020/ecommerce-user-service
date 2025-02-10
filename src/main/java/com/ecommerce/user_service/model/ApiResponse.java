package com.ecommerce.user_service.model;

import com.ecommerce.user_service.enums.ApiResponseStatusEnum;
import lombok.Getter;


@Getter
public abstract class ApiResponse
{
  private final ApiResponseStatusEnum status;
  
  public ApiResponse (ApiResponseStatusEnum status)
  {
    this.status = status;
  }
}