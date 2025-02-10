package com.ecommerce.user_service.model;

import com.ecommerce.user_service.entity.UserEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

@Getter
@Setter
public class CustomUserDetails extends User
{
  private UserEntity userEntity;
  public CustomUserDetails (UserEntity userEntity)
  {
    super(userEntity.getName (),
          userEntity.getPassword (),
         userEntity.isEnabled (),
         true,
         true,
         !userEntity.isLocked (),
         List.of (new SimpleGrantedAuthority (userEntity.getRole ())));
    this.userEntity = userEntity;
  }
  
}
