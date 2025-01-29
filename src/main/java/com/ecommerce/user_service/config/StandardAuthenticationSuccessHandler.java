package com.ecommerce.user_service.config;

import com.ecommerce.user_service.jwt.JwtTokenHandler;
import com.ecommerce.user_service.service.RefreshTokenService;
import com.ecommerce.user_service.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class StandardAuthenticationSuccessHandler extends JwtTokenHandler implements AuthenticationSuccessHandler
{
  
  public StandardAuthenticationSuccessHandler (JwtUtil jwtUtil, RefreshTokenService refreshTokenService)
  {
    super (jwtUtil, refreshTokenService);
  }
  
  @Override public void onAuthenticationSuccess (HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws
                                                                                                                                          IOException
  {
      onSuccess (request, response, (UserDetails) authentication.getPrincipal ());
    
  }

}
