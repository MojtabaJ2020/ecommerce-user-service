package com.ecommerce.user_service.service;

import com.ecommerce.user_service.entity.RefreshTokenEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public interface RefreshTokenService
{
  Optional<RefreshTokenEntity> findByToken(String token);
  RefreshTokenEntity save (RefreshTokenEntity refreshTokenEntity);
  String refreshAccessToken (String refreshToken) throws Exception;
  void revokeRefreshToken(String refreshToken);
  void storeRefreshToken (HttpServletRequest request, UserDetails userDetails, String token);
  
}
