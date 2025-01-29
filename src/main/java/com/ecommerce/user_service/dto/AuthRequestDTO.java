package com.ecommerce.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @NoArgsConstructor @AllArgsConstructor public class AuthRequestDTO
{
  private String username;
  private String password;
}
