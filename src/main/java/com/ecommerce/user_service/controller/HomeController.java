package com.ecommerce.user_service.controller;

import com.ecommerce.user_service.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
  
  @Autowired
  private JwtUtil jwtUtil; // Ensure this validates the JWT
  
  @GetMapping("/home")
  public String home( Model model) {
      return "home";
  }
}

