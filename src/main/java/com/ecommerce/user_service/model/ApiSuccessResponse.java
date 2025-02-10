package com.ecommerce.user_service.model;

import com.ecommerce.user_service.enums.ApiResponseStatusEnum;
import lombok.Getter;

import java.util.List;

@Getter
public class ApiSuccessResponse<T> extends ApiResponse
{
  private String message;
  private List <T> data;
  private  MetaData metadata;
  
  private ApiSuccessResponse ()
  {
    super (ApiResponseStatusEnum.SUCCESS);
  }
  
  public ApiSuccessResponse (String message)
  {
    this (message, null, null);
  }
  
  public ApiSuccessResponse ( String message, List<T> data)
  {
    this (message, data, null);
  }
  
  public ApiSuccessResponse (String message, List <T> data, MetaData metadata)
  {
    this();
    this.message = message;
    this.data = data;
    this.metadata = metadata;
  }
}