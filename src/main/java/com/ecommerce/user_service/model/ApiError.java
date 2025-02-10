package com.ecommerce.user_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ApiError {
  private final String errorCode;
  private final String message;
  private final String debugMessage;
  private final String help;
  private final List <ApiSubError> subErrors;
}
