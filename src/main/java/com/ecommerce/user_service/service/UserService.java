package com.ecommerce.user_service.service;

import com.ecommerce.user_service.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface UserService
{
  UserEntity register (UserEntity userEntity);
  
  Optional<UserEntity> findByUsername(String userName);
  
  Optional<UserEntity> findByEmail(String email);
  
  List <UserEntity> findAll();
  
  UserEntity update(UserEntity userEntity);
  
}
