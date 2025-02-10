package com.ecommerce.user_service.service.impl;

import com.ecommerce.user_service.entity.UserEntity;
import com.ecommerce.user_service.entity.VerificationToken;
import com.ecommerce.user_service.property.VerificationTokenProperties;
import com.ecommerce.user_service.repo.VerificationTokenRepository;
import com.ecommerce.user_service.service.VerificationTokenService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class VerificationTokenServiceImpl implements VerificationTokenService
{
  private final VerificationTokenRepository repository;
  private final VerificationTokenProperties verificationTokenProperties;
  
  public VerificationTokenServiceImpl (VerificationTokenRepository repository, VerificationTokenProperties verificationTokenProperties)
  {
    this.repository = repository;
    this.verificationTokenProperties = verificationTokenProperties;
  }
  
  @Override public void create (UserEntity userEntity, String token)
  {
    LocalDateTime expiresAt = LocalDateTime.now ().plusMinutes (this.verificationTokenProperties.getExpiration ());
    VerificationToken verificationToken = VerificationToken.builder ().userEntity (userEntity).token (token).expiresAt (expiresAt).build ();
    this.repository.save (verificationToken);
  }
  
  @Override public Optional <VerificationToken> findByToken (String token)
  {
    return repository.findByToken (token);
  }
}
