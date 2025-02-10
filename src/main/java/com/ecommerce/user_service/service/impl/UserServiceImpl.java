package com.ecommerce.user_service.service.impl;

import com.ecommerce.user_service.entity.UserEntity;
import com.ecommerce.user_service.enums.RoleEnum;
import com.ecommerce.user_service.exception.DuplicateUserException;
import com.ecommerce.user_service.repo.UserRepository;
import com.ecommerce.user_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService
{
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  
  @Autowired
  public UserServiceImpl (UserRepository userRepository, PasswordEncoder passwordEncoder)
  {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }
  
  @Override public UserEntity register (UserEntity userEntity)
  {
    if(userRepository.findByName (userEntity.getName ()).isPresent ())
    {
      throw new DuplicateUserException ("Already exist a user with the same username.");
    }
    if(userRepository.findByEmail (userEntity.getEmail ()).isPresent ())
    {
      throw new DuplicateUserException ("Already exist a user with the same email.");
    }
    userEntity.setPassword (passwordEncoder.encode (userEntity.getPassword ()));
    userEntity.setRole (RoleEnum.USER.toString ());
    userEntity.setEnabled (true);
    userEntity.setLocked (false);
    userEntity.setPendingActivation (true);
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
  
  @Override public UserEntity update (UserEntity userEntity)
  {
    return userRepository.save (userEntity);
  }
}
