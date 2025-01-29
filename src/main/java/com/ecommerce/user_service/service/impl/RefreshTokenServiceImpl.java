package com.ecommerce.user_service.service.impl;

import com.ecommerce.user_service.entity.RefreshTokenEntity;
import com.ecommerce.user_service.entity.UserEntity;
import com.ecommerce.user_service.enums.TokenProviderEnum;
import com.ecommerce.user_service.jwt.JwtUtil;
import com.ecommerce.user_service.repo.RefreshTokenRepository;
import com.ecommerce.user_service.service.RefreshTokenService;
import com.ecommerce.user_service.service.UserService;
import com.ecommerce.user_service.util.AESUtil;
import com.ecommerce.user_service.util.DateTimeUtil;
import com.ecommerce.user_service.util.ServletUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service public class RefreshTokenServiceImpl implements RefreshTokenService
{
  
  @Autowired private RefreshTokenRepository refreshTokenRepository;
  @Autowired private JwtUtil jwtUtil;
  @Autowired private AESUtil aesUtil;
  @Autowired private UserDetailsService userDetailsService;
  @Autowired private UserService userService;
  
  @Override public Optional <RefreshTokenEntity> findByToken (String token)
  {
    return Optional.empty ();
  }
  
  @Override public RefreshTokenEntity save (RefreshTokenEntity refreshTokenEntity)
  {
    return refreshTokenRepository.save (refreshTokenEntity);
  }
  
  @Override public void storeRefreshToken (HttpServletRequest request, UserDetails userDetails, String token)
  {
    Optional <UserEntity> userEntity = userService.findByUsername (userDetails.getUsername ());
    RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder ()
                                                              .provider (TokenProviderEnum.INTERNAL.toString ())
                                                              .ipAddress (ServletUtil.getClientIpAddress (request))
                                                              .expiresAt (DateTimeUtil.toLocalDateTime (jwtUtil.extractExpiration (token)))
                                                              .createdAt (DateTimeUtil.toLocalDateTime (jwtUtil.extractIssueDate (token)))
                                                              .token (aesUtil.encrypt (token))
                                                              .revoked (false)
                                                              .lastUsedAt (null)
                                                              .userEntity (userEntity.get ())
                                                              .build ();
    save (refreshTokenEntity);
  }
  
  @Override public String refreshAccessToken (String refreshToken)
  {
    String encryptedToken = aesUtil.encrypt (refreshToken);
    List <RefreshTokenEntity> tokenEntities = this.refreshTokenRepository.findByToken (encryptedToken);
    if (tokenEntities == null || tokenEntities.isEmpty ())
    {
      throw new EntityNotFoundException ("Token not found");
    }
    RefreshTokenEntity refreshTokenEntity = tokenEntities.get (0);
    if (refreshTokenEntity.isRevoked ())
    {
      throw new RuntimeException ("Invalid or expired refresh token");
    }
    if (Objects.equals (refreshTokenEntity.getProvider (), TokenProviderEnum.INTERNAL.toString ()))
    {
      if (jwtUtil.validateToken (refreshToken))
      {
        String username = jwtUtil.extractUsername (refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername (username);
        String newAccessToken = jwtUtil.generateAccessToken (userDetails);
        refreshTokenEntity.setLastUsedAt (LocalDateTime.now ());
        save (refreshTokenEntity);
        return newAccessToken;
      }
      else
      {
        throw new RuntimeException ("Invalid or expired refresh token");
      }
    }
    else
    {
      return null;
    }
  }
  
  @Override public void revokeRefreshToken (String refreshToken)
  {
  
  }
}
