package com.ecommerce.user_service.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class DateTimeUtil
{
  public static LocalDateTime toLocalDateTime(Date date)
  {
    return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    
  }
}
