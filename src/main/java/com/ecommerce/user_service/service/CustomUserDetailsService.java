package com.ecommerce.user_service.service;

import com.ecommerce.user_service.entity.UserEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

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
    return User.builder ()
               .username (username)
               .password (userEntity.getPassword ())
               .authorities (List.of (new SimpleGrantedAuthority (userEntity.getRole ().toString ())))
               .build ();
  }
}
