package com.ecommerce.user_service.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties (prefix = "security.jwt")
@Getter
@Setter
public class JwtProperties
{
  private String secret;
  private long refreshTokenExpiration;
  private long accessTokenExpiration;
}
