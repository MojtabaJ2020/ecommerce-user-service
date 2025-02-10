package com.ecommerce.user_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class AuthRequestDTO
{
  
  @NotNull(message = "user.name.not.null")
  @Size (min = 3, max = 64, message = "user.name.not.allowed.size")
  private String username;
  
  @NotNull(message = "user.password.not.null")
  @Size (min = 3, max = 64, message = "user.password.not.allowed.size")
  private String password;
}
