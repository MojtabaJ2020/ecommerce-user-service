package com.ecommerce.user_service.controller;

import com.ecommerce.user_service.model.CustomUserDetails;
import com.ecommerce.user_service.service.UserService;
import com.ecommerce.user_service.util.DTOUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController
{
  private final UserService userService;
  
  @Autowired
  public UserController (UserService userService)
  {
    this.userService = userService;
  }
  
  @GetMapping("")
  public ResponseEntity<?> getUsers()
  {
  return new ResponseEntity <> (DTOUtil.toDTOs (userService.findAll ()),HttpStatus.OK);
  }
  
  @GetMapping("/me")
  public ResponseEntity<?> getUserDetails(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
    if (customUserDetails == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
    }
    return ResponseEntity.ok(DTOUtil.toDTO (customUserDetails.getUserEntity ()));
  }
  
}
