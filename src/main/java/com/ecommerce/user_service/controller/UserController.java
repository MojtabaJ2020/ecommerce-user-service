package com.ecommerce.user_service.controller;

import com.ecommerce.user_service.dto.UserDTO;
import com.ecommerce.user_service.entity.UserEntity;
import com.ecommerce.user_service.service.UserService;
import com.ecommerce.user_service.util.DTOUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController
{
  private final UserService userService;
  
  public UserController (UserService userService)
  {
    this.userService = userService;
  }
  
  @PostMapping("/register")
  public ResponseEntity<?> registerUser(@RequestBody @Valid UserDTO userDTO)
  {
    UserEntity userEntity = this.userService.register (DTOUtil.toEntity (userDTO));
    return new ResponseEntity <> (DTOUtil.toDTO (userEntity), HttpStatus.CREATED);
  }
  @GetMapping("")
  public ResponseEntity<?> getUsers()
  {
  return new ResponseEntity <> (DTOUtil.toDTOs (userService.findAll ()),HttpStatus.OK);
  }

}
