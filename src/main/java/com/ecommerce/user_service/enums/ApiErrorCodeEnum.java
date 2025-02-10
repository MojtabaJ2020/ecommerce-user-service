package com.ecommerce.user_service.enums;

import lombok.Getter;

@Getter public enum ApiErrorCodeEnum
{
  INVALID_REFRESH_TOKEN("INV_600", "Invalid refresh token"),
  NOT_SUPPORTED("SUP_601","Not supported"),
  ENCRYPT_DECRYPT("ENC_601","Unable to encrypt or decrypt data"),
  
  USER_NOT_FOUND("USR_404", "User not found"),
  USER_DUPLICATE("USR_409", "Duplicate username or email"),
  USER_INACTIVE("USR_601", "User not activated"),
  INVALID_INPUT("INV_400", "Invalid input provided"),
  IO_ERROR("IO_400", "Could not read/load file"),
  UNAUTHORIZED("AUTH_401", "Unauthorized access"),
  INTERNAL_ERROR("INT_500", "An unexpected error occurred");

  
  private final String code;
  private final String message;
  
  ApiErrorCodeEnum(String code, String message) {
    this.code = code;
    this.message = message;
  }
  
}
