package com.ecommerce.user_service.service.impl;

import com.ecommerce.user_service.entity.UserEntity;
import com.ecommerce.user_service.repo.UserRepository;
import com.ecommerce.user_service.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService
{
  private final UserRepository userRepository;
  
  public UserServiceImpl (UserRepository userRepository)
  {
    this.userRepository = userRepository;
  }
  
  @Override public UserEntity register (UserEntity userEntity)
  {
    return userRepository.save (userEntity);
  }
  
  @Override public Optional <UserEntity> findByUsername (String userName)
  {
    return userRepository.findByName (userName);
  }
  
  @Override public Optional <UserEntity> findByEmail (String email)
  {
    return userRepository.findByEmail (email);
  }
  
  @Override public List <UserEntity> findAll ()
  {
    return userRepository.findAll ();
  }
}
