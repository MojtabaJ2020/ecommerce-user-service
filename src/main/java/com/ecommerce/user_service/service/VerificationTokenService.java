package com.ecommerce.user_service.service;

import com.ecommerce.user_service.entity.UserEntity;
import com.ecommerce.user_service.entity.VerificationToken;

import java.util.Optional;

public interface VerificationTokenService
{
  void create (UserEntity userEntity, String token);
  
  Optional<VerificationToken> findByToken(String token);
}
