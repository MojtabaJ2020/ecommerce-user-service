package com.ecommerce.user_service.jwt;

import com.ecommerce.user_service.enums.TokenTypeEnum;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter
{
  private final JwtUtil jwtUtil;
  private final UserDetailsService userDetailsService;
  private final Logger logger = LoggerFactory.getLogger (JwtAuthenticationFilter.class);
  
  public JwtAuthenticationFilter (UserDetailsService userDetailsService, JwtUtil jwtUtil)
  {
    this.userDetailsService = userDetailsService;
    this.jwtUtil = jwtUtil;
  }
  
  @Override protected void doFilterInternal (HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws
                                                                                                                                ServletException,
                                                                                                                                IOException
  {
    // Look for the JWT access token in the request header/cookie
    String jwtAccessToken = extractJwtFromRequestHeader (request);
    if (jwtAccessToken == null)
    {
      jwtAccessToken = extractJwtFromRequestCooke (request);
    }
    if (jwtAccessToken != null)
    {
      if (jwtUtil.validateToken (jwtAccessToken))
      {
        String username = jwtUtil.extractUsername (jwtAccessToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername (username);
        
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken (userDetails, null, userDetails.getAuthorities ());
        
        token.setDetails (new WebAuthenticationDetails (request));
        SecurityContextHolder.getContext ().setAuthentication (token);
      }
      else
      {
        // add a log
        logger.info ("JWT access token is invalid!");
      }
    }
    
    filterChain.doFilter (request, response);
  }
  
  private String extractJwtFromRequestCooke (HttpServletRequest request)
  {
    Cookie cookie = WebUtils.getCookie (request, TokenTypeEnum.ACCESS_TOKEN.toString ());
    return cookie == null ? null : cookie.getValue ();
  }
  
  private String extractJwtFromRequestHeader (HttpServletRequest request)
  {
    String bearerToken = request.getHeader ("Authorization");
    if (bearerToken != null && bearerToken.startsWith ("Bearer "))
    {
      return bearerToken.substring (7);
    }
    return null;
  }
}
