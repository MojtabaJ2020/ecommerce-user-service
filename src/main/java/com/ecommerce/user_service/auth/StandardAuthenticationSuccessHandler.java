package com.ecommerce.user_service.auth;

import com.ecommerce.user_service.service.JwtService;
import com.ecommerce.user_service.service.RefreshTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component public class StandardAuthenticationSuccessHandler extends AbstractStatelessAuthenticationSuccessHandler
{
  
  public StandardAuthenticationSuccessHandler (JwtService jwtService, RefreshTokenService refreshTokenService)
  {
    super (jwtService, refreshTokenService);
  }
  
  @Override public void onAuthenticationSuccess (HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws
                                                                                                                                          IOException,
                                                                                                                                          ServletException
  {
    onSuccess (request, response, authentication, (UserDetails) authentication.getPrincipal ());
  }
}
