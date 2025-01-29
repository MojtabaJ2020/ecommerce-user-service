package com.ecommerce.user_service.enums;

import java.util.stream.Stream;

public enum TokenTypeEnum
{
  ACCESS_TOKEN("access_token"),
  REFRESH_TOKEN("refresh_token");
  private final String value;
  
  TokenTypeEnum (String value)
  {
    this.value = value;
  }
  
  @Override public String toString ()
  {
    return  value;
  }
  
  public static TokenTypeEnum toEnum (String value)
  {
    if (value == null)
    {
      return null;
    }
    return Stream.of (values ()).filter (token -> token.toString ().equals (value)).findFirst ().orElse (null);
  }
}
