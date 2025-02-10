package com.ecommerce.user_service.service.impl;

import com.ecommerce.user_service.entity.RefreshToken;
import com.ecommerce.user_service.entity.UserEntity;
import com.ecommerce.user_service.enums.TokenProviderEnum;
import com.ecommerce.user_service.exception.InvalidRefreshTokenException;
import com.ecommerce.user_service.exception.NotSupportedException;
import com.ecommerce.user_service.exception.RefreshTokenNotFoundException;
import com.ecommerce.user_service.exception.UserNotFoundException;
import com.ecommerce.user_service.repo.RefreshTokenRepository;
import com.ecommerce.user_service.service.JwtService;
import com.ecommerce.user_service.service.LogSanitizerService;
import com.ecommerce.user_service.service.RefreshTokenService;
import com.ecommerce.user_service.service.UserService;
import com.ecommerce.user_service.util.AESUtil;
import com.ecommerce.user_service.util.DateTimeUtil;
import com.ecommerce.user_service.util.ServletUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j @Service public class RefreshTokenServiceImpl implements RefreshTokenService
{
  
  @Autowired private RefreshTokenRepository refreshTokenRepository;
  @Autowired private JwtService jwtService;
  @Autowired private AESUtil aesUtil;
  @Autowired private UserDetailsService userDetailsService;
  @Autowired private UserService userService;
  @Autowired private LogSanitizerService sanitizerService;
  
  @Override public List <RefreshToken> findByToken (String token)
  {
    return this.refreshTokenRepository.findByToken (token);
  }
  
  @Override public RefreshToken update (RefreshToken refreshToken)
  {
    return refreshTokenRepository.save (refreshToken);
  }
  
  @Override public void storeRefreshToken (HttpServletRequest request, UserDetails userDetails, String token)
  {
    Optional <UserEntity> userEntity = userService.findByUsername (userDetails.getUsername ());
    if (userEntity.isEmpty ())
    {
      throw new UserNotFoundException ("User not found!");
    }
    RefreshToken refreshToken = RefreshToken.builder ()
                                            .provider (TokenProviderEnum.INTERNAL.toString ())
                                            .ipAddress (ServletUtil.getClientIpAddress (request))
                                            .expiresAt (DateTimeUtil.toLocalDateTime (jwtService.extractExpiration (token)))
                                            .createdAt (DateTimeUtil.toLocalDateTime (jwtService.extractIssueDate (token)))
                                            .token (aesUtil.encrypt (token))
                                            .revoked (false)
                                            .lastUsedAt (null)
                                            .userEntity (userEntity.get ())
                                            .build ();
    update (refreshToken);
  }
  
  @Override public String refreshAccessToken (String refreshToken) throws RefreshTokenNotFoundException,InvalidRefreshTokenException
  {
    String encryptedToken = aesUtil.encrypt (refreshToken);
    List <RefreshToken> tokenEntities = this.refreshTokenRepository.findByToken (encryptedToken);
    if (tokenEntities == null || tokenEntities.isEmpty ())
    {
      throw new RefreshTokenNotFoundException ("Refresh token not found.");
    }
    RefreshToken refreshTokenEntity = tokenEntities.get (0);
    if (refreshTokenEntity.isRevoked ())
    {
      throw new InvalidRefreshTokenException ("Refresh token has been revoked");
    }
    if (Objects.equals (refreshTokenEntity.getProvider (), TokenProviderEnum.INTERNAL.toString ()))
    {
      if (jwtService.isTokenValid (refreshToken))
      {
        String username = jwtService.extractUsername (refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername (username);
        String newAccessToken = jwtService.generateAccessToken (userDetails);
        refreshTokenEntity.setLastUsedAt (LocalDateTime.now ());
        update (refreshTokenEntity);
        return newAccessToken;
      }
      else
      {
        throw new InvalidRefreshTokenException ("Invalid or expired refresh token");
      }
    }
    else
    {
      throw new NotSupportedException ("Cannot refresh access token because token provider is not internal");
    }
  }
  
  @Override public void revokeRefreshToken (String refreshToken)
  {
  
  }
}
