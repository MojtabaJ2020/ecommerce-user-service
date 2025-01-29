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
  private UserDTO()
  {
  }
  @NotNull(message = "Name can not be null!")
  @Size (min = 3, max = 64, message = "Name must be between 3 and 64 characters!")
  private String name;
  
  @NotNull(message = "Email can not be null!")
  private String email;
  
  @NotNull(message = "Password can not be null!")
  @Size (min = 3, max = 64, message = "Password must be between 3 and 64 characters!")
  private String password;
  
  @NotNull(message = "Role can not be null!")
  @Size (min = 3, max = 64, message = "Role must be between 3 and 64 characters!")
  private String role;
  
  private MultipartFile picture;
}
