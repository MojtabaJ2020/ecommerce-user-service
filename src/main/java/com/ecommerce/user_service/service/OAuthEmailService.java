package com.ecommerce.user_service.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.*;
import java.util.List;
import java.util.Map;

@Service
public class OAuthEmailService
{
  private final RestTemplate restTemplate;
  
  public OAuthEmailService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }
  
  public String fetchPrimaryEmail(String url, String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + accessToken);
    headers.setContentType(MediaType.APPLICATION_JSON);
    
    HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
    
    ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
        url, HttpMethod.GET, requestEntity,(Class<List<Map<String, Object>>>) (Class<?>) List.class);
    
    if (response.getStatusCode() == HttpStatus.OK) {
      List<Map<String, Object>> emails = response.getBody();
      if (emails != null) {
        for (Map<String, Object> emailEntry : emails) {
          if (Boolean.TRUE.equals(emailEntry.get("primary")) &&
              Boolean.TRUE.equals(emailEntry.get("verified"))) {
            return (String) emailEntry.get("email");
          }
        }
      }
    }
    
    throw new RuntimeException("Unable to fetch primary email.");
  }
}

