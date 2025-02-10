package com.ecommerce.user_service.service;

import com.ecommerce.user_service.entity.UserEntity;
import com.ecommerce.user_service.model.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService
{
  private final UserService userService;
  
  public CustomUserDetailsService (UserService userService)
  {
    this.userService = userService;
  }
  
  @Override public UserDetails loadUserByUsername (String username) throws UsernameNotFoundException
  {
    UserEntity userEntity = this.userService.findByUsername (username).orElseThrow (() -> new UsernameNotFoundException (username));
    return new CustomUserDetails(userEntity);
  }
}
