package com.ecommerce.user_service.listener;

import com.ecommerce.user_service.entity.UserEntity;
import com.ecommerce.user_service.event.OnRegistrationCompleteEvent;
import com.ecommerce.user_service.property.CommonProperties;
import com.ecommerce.user_service.service.MailService;
import com.ecommerce.user_service.service.VerificationTokenService;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RegistrationListener implements ApplicationListener <OnRegistrationCompleteEvent>
{

  private final VerificationTokenService verificationTokenService;
  private final MailService mailService;
  private final CommonProperties commonProperties;
  
  public RegistrationListener (VerificationTokenService verificationTokenService, MailService mailService, CommonProperties commonProperties)
  {
    this.verificationTokenService = verificationTokenService;
    this.mailService = mailService;
    this.commonProperties = commonProperties;
  }
  
  @Override
  public void onApplicationEvent(OnRegistrationCompleteEvent event)
  {
    UserEntity userEntity = event.getUserEntity ();
    String token = UUID.randomUUID ().toString ();
    verificationTokenService.create (userEntity, token);
    
    mailService.sendUserActivationToken (userEntity.getEmail (), commonProperties.getBaseUrl (), token, event.getLocale ());
  }
}
