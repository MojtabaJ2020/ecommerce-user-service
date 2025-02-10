package com.ecommerce.user_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Builder
@AllArgsConstructor
@Getter
public class UserDTO
{
  @NotNull(message = "user.name.not.null")
  @Size (min = 3, max = 64, message = "user.name.not.allowed.size")
  private String name;
  
  @NotNull(message = "user.email.not.null")
  @Size (min = 3, max = 255, message = "user.email.not.allowed.size")
  private String email;
  
  @NotNull(message = "user.password.not.null")
  @Size (min = 3, max = 64, message = "user.password.not.allowed.size")
  private String password;
  
  @NotNull(message = "user.role.not.null")
  @Size (min = 3, max = 64, message = "user.role.not.allowed.size")
  private String role;
  
  private MultipartFile picture;
}
