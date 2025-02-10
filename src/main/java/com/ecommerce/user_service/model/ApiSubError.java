package com.ecommerce.user_service.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder public class ApiSubError
{
  String object;
  String field;
  String message;
}
