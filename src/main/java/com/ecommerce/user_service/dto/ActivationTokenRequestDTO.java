package com.ecommerce.user_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class ActivationTokenRequestDTO
{
  @NotNull(message = "user.email.not.null")
  @Size (min = 3, max = 255, message = "user.email.not.allowed.size")
  private String email;
}
