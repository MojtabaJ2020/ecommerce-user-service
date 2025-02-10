package com.ecommerce.user_service.jwt;

import com.ecommerce.user_service.enums.TokenTypeEnum;
import com.ecommerce.user_service.service.JwtService;
import com.ecommerce.user_service.service.RefreshTokenService;
import com.ecommerce.user_service.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;

public class JwtTokenHandler
{
  private final JwtService jwtService;
  
  private final RefreshTokenService refreshTokenService;
  
  public JwtTokenHandler (JwtService jwtService, RefreshTokenService refreshTokenService)
  {
    this.jwtService = jwtService;
    this.refreshTokenService = refreshTokenService;
  }
  
  public final void onSuccess (HttpServletRequest request, HttpServletResponse response, UserDetails userDetails) throws IOException
  {
    handleAccessToken (request, response, userDetails);
    handleRefreshToken (request, response, userDetails);
  }
  
  public void handleAccessToken (HttpServletRequest request, HttpServletResponse response, UserDetails userDetails)
  {
    String accessToken = jwtService.generateAccessToken (userDetails);
    injectAccessToken (response, accessToken);
  }
  
  public void handleRefreshToken (HttpServletRequest request, HttpServletResponse response, UserDetails userDetails)
  {
    String refreshToken = jwtService.generateRefreshToken (userDetails);
    refreshTokenService.storeRefreshToken (request, userDetails, refreshToken);
    injectRefreshToken (response, refreshToken);
  }
  
  protected void injectAccessToken (HttpServletResponse response, String token)
  {
    response.setHeader ("Authorization", "Bearer " + token);
    response.addCookie (CookieUtil.generate (token, TokenTypeEnum.ACCESS_TOKEN,Long.valueOf (jwtService.getProperties ().getAccessTokenExpiration ()).intValue ()));
  }
  
  protected void injectRefreshToken (HttpServletResponse response, String token)
  {
    response.setHeader (TokenTypeEnum.REFRESH_TOKEN.toString (), token);
    response.addCookie (CookieUtil.generate (token, TokenTypeEnum.REFRESH_TOKEN,Long.valueOf (jwtService.getProperties ().getRefreshTokenExpiration ()).intValue ()));
  }
}
