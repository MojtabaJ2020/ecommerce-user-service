package com.ecommerce.user_service.service;

import com.ecommerce.user_service.entity.RefreshToken;
import com.ecommerce.user_service.exception.InvalidRefreshTokenException;
import com.ecommerce.user_service.exception.RefreshTokenNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface RefreshTokenService
{
  List <RefreshToken> findByToken(String token);
  
  RefreshToken update (RefreshToken refreshToken);
  
  String refreshAccessToken (String refreshToken) throws RefreshTokenNotFoundException, InvalidRefreshTokenException;
  
  void revokeRefreshToken(String refreshToken);
  
  void storeRefreshToken (HttpServletRequest request, UserDetails userDetails, String token);
  
}
