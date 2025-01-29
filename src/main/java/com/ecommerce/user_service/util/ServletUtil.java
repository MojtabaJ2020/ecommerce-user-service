package com.ecommerce.user_service.util;

import jakarta.servlet.http.HttpServletRequest;

public class ServletUtil
{
  public static String getClientIpAddress (HttpServletRequest request)
  {
    String ipAddress = request.getHeader ("X-Forwarded-For");
    if (ipAddress == null || ipAddress.isEmpty () || "unknown".equalsIgnoreCase (ipAddress))
    {
      ipAddress = request.getHeader ("Proxy-Client-IP");
    }
    if (ipAddress == null || ipAddress.isEmpty () || "unknown".equalsIgnoreCase (ipAddress))
    {
      ipAddress = request.getHeader ("WL-Proxy-Client-IP");
    }
    if (ipAddress == null || ipAddress.isEmpty () || "unknown".equalsIgnoreCase (ipAddress))
    {
      ipAddress = request.getRemoteAddr ();
    }
    // If multiple IPs are in the X-Forwarded-For header, take the first one
    if (ipAddress != null && ipAddress.contains (","))
    {
      ipAddress = ipAddress.split (",")[0];
    }
    return ipAddress;
  }
}
