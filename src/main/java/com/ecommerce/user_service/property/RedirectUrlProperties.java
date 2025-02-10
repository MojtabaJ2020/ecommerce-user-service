package com.ecommerce.user_service.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter @Setter @Configuration @ConfigurationProperties (prefix = "redirect-url") public class RedirectUrlProperties
{
  private String afterSuccessLogin;
  private String afterFailureLogin;
  private String afterSuccessActivation;
  private String afterFailureActivation;
}
