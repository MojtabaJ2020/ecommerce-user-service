package com.ecommerce.user_service.event;

import com.ecommerce.user_service.entity.UserEntity;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Locale;

@Getter
public class OnRegistrationCompleteEvent extends ApplicationEvent
{
  private final Locale locale;
  private final UserEntity userEntity;
  
  public OnRegistrationCompleteEvent (UserEntity userEntity, Locale locale)
  {
    super (userEntity);
    this.locale = locale;
    this.userEntity = userEntity;
  }
}
