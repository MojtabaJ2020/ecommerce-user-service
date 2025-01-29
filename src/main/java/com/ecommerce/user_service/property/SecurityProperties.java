package com.ecommerce.user_service.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Setter @Getter @Configuration
@ConfigurationProperties (prefix = "security")
public class SecurityProperties {
  
  private List <String> whitelistedEndpoints;
  
}