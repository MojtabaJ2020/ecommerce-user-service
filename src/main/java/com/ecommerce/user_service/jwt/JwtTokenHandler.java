package com.ecommerce.user_service.jwt;

import com.ecommerce.user_service.enums.TokenTypeEnum;
import com.ecommerce.user_service.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;

public class JwtTokenHandler
{
  private final JwtUtil jwtUtil;
  
  private final RefreshTokenService refreshTokenService;
  
  public JwtTokenHandler (JwtUtil jwtUtil, RefreshTokenService refreshTokenService)
  {
    this.jwtUtil = jwtUtil;
    this.refreshTokenService = refreshTokenService;
  }
  
  public final void onSuccess (HttpServletRequest request, HttpServletResponse response, UserDetails userDetails) throws IOException
  {
    handleAccessToken (request, response, userDetails);
    handleRefreshToken (request, response, userDetails);
    response.sendRedirect ("/home");
  }
  
  public void handleAccessToken (HttpServletRequest request, HttpServletResponse response, UserDetails userDetails)
  {
    String accessToken = jwtUtil.generateAccessToken (userDetails);
    injectAccessToken (response, accessToken);
  }
  
  public void handleRefreshToken (HttpServletRequest request, HttpServletResponse response, UserDetails userDetails)
  {
    String refreshToken = jwtUtil.generateRefreshToken (userDetails);
    refreshTokenService.storeRefreshToken (request, userDetails, refreshToken);
    injectRefreshToken (response, refreshToken);
  }
  
  protected void injectAccessToken (HttpServletResponse response, String token)
  {
    response.setHeader ("Authorization", "Bearer " + token);
    response.addCookie (generateCookie (token, TokenTypeEnum.ACCESS_TOKEN));
  }
  
  protected void injectRefreshToken (HttpServletResponse response, String token)
  {
    response.setHeader (TokenTypeEnum.REFRESH_TOKEN.toString (), token);
    response.addCookie (generateCookie (token, TokenTypeEnum.REFRESH_TOKEN));
  }
  
  private Cookie generateCookie (String token, TokenTypeEnum tokenTypeEnum)
  {
    Cookie cookie = new Cookie (tokenTypeEnum.toString (), token);
    cookie.setHttpOnly (true); // Prevent JavaScript access
    cookie.setSecure (true);  // Set to true if using HTTPS
    cookie.setPath ("/");  // Make cookie accessible for the entire domain
    return cookie;
  }
}
