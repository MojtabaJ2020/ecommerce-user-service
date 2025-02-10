package com.ecommerce.user_service.util;

import com.ecommerce.user_service.enums.TokenTypeEnum;
import jakarta.servlet.http.Cookie;

public class CookieUtil
{
  public static Cookie generate (String token, TokenTypeEnum tokenTypeEnum, long maxAgeInMillis)
  {
    Cookie cookie = new Cookie (tokenTypeEnum.toString (), token);
    cookie.setHttpOnly (true); // Prevent JavaScript access
    cookie.setSecure (false);  // Set to true if using HTTPS
    cookie.setPath ("/");  // Make cookie accessible for the entire
    cookie.setMaxAge ((int)maxAgeInMillis/1000);
    return cookie;
  }
}
