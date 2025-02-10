package com.ecommerce.user_service.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter @Setter @Configuration @ConfigurationProperties (prefix = "common-params") public class CommonProperties
{
  private String baseUrl;
  private String frontBaseUrl;
}
