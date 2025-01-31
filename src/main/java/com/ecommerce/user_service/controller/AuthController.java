package com.ecommerce.user_service.controller;

import com.ecommerce.user_service.dto.AuthRequestDTO;
import com.ecommerce.user_service.dto.JwtTokenDTO;
import com.ecommerce.user_service.dto.UserDTO;
import com.ecommerce.user_service.entity.UserEntity;
import com.ecommerce.user_service.enums.TokenTypeEnum;
import com.ecommerce.user_service.service.RefreshTokenService;
import com.ecommerce.user_service.jwt.JwtUtil;
import com.ecommerce.user_service.service.UserService;
import com.ecommerce.user_service.util.DTOUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;

@Controller public class AuthController
{
  private final AuthenticationManager authenticationManager;
  private final JwtUtil jwtUtil;
  private final RefreshTokenService refreshTokenService;
  private final UserService userService;
  
  public AuthController (AuthenticationManager authenticationManager, JwtUtil jwtUtil, RefreshTokenService refreshTokenService, UserService userService)
  {
    this.authenticationManager = authenticationManager;
    this.jwtUtil = jwtUtil;
    this.refreshTokenService = refreshTokenService;
    this.userService = userService;
  }
  
  @GetMapping ("/login") public String login ()
  {
    return "login";
  }
  
  @PostMapping ("/authenticate") public ResponseEntity <?> authenticate (HttpServletRequest request, @RequestBody AuthRequestDTO authRequestDTO)
  {
    Authentication authentication = authenticationManager.authenticate (new UsernamePasswordAuthenticationToken (authRequestDTO.getUsername (),
                                                                                                                 authRequestDTO.getPassword ()));
    
    UserDetails userDetails = (UserDetails) authentication.getPrincipal ();
    String accessToken = jwtUtil.generateAccessToken (userDetails);
    String refreshToken = jwtUtil.generateRefreshToken (userDetails);
    refreshTokenService.storeRefreshToken (request, userDetails, refreshToken);
    return ResponseEntity.ok (JwtTokenDTO.builder ().accessToken (accessToken).refreshToken (refreshToken).build ());
  }
  
  @PostMapping ("/refresh-token") public ResponseEntity <?> refreshToken (@RequestBody JwtTokenDTO jwtTokenDTO)
  {
    if (jwtTokenDTO == null || jwtTokenDTO.getRefreshToken () == null)
    {
      return new ResponseEntity <> ("Invalid parameter!", HttpStatus.BAD_REQUEST);
    }
    try
    {
      String accessToken = this.refreshTokenService.refreshAccessToken (jwtTokenDTO.getRefreshToken ());
      return new ResponseEntity<> (JwtTokenDTO.builder ().accessToken (accessToken).refreshToken (jwtTokenDTO.getRefreshToken ()).build (), HttpStatus.OK);
    }
    catch (Exception ex)
    {
      return ResponseEntity.status (403).body ("Invalid or expired refresh token");
    }
  }
  
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletResponse response) {
    Cookie jwtCookie = new Cookie(TokenTypeEnum.ACCESS_TOKEN.toString (), null);
    jwtCookie.setPath("/login");
    jwtCookie.setMaxAge(0);
    jwtCookie.setHttpOnly(true);
    response.addCookie(jwtCookie);
    return ResponseEntity.ok().build();
  }
  
  @PostMapping("/register")
  public ResponseEntity<?> registerUser(@RequestBody @Valid UserDTO userDTO)
  {
    UserEntity userEntity = this.userService.register (DTOUtil.toEntity (userDTO));
    return new ResponseEntity <> (DTOUtil.toDTO (userEntity), HttpStatus.CREATED);
  }
}
