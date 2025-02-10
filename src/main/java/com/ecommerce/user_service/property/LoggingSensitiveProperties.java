package com.ecommerce.user_service.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @Configuration @ConfigurationProperties (prefix = "logging.sensitive") public class LoggingSensitiveProperties
{
  private List<String> exactRedactedKeys = new ArrayList <> ();
  private List<String> patternRedactedKeys = new ArrayList<>();
  private List<String> patternRedactedValues = new ArrayList<>();
}
