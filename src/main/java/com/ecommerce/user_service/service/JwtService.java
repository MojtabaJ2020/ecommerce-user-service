package com.ecommerce.user_service.service;

import com.ecommerce.user_service.property.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JwtService
{
  private static final int MIN_SECRET_LENGTH = 32; // 256-bit for HS256
  @Getter
  private final JwtProperties properties;
  private final UserDetailsService userDetailsService;
  private final LogSanitizerService sanitizerService;
  private final SecretKey signingKey;
  
  @Autowired
  public JwtService (JwtProperties properties, UserDetailsService userDetailsService, LogSanitizerService sanitizerService) {
    this.properties = properties;
    this.userDetailsService = userDetailsService;
    this.sanitizerService = sanitizerService;
    this.signingKey = validateAndCreateSigningKey();
  }
  
  private SecretKey validateAndCreateSigningKey() {
    if (properties.getSecret() == null || properties.getSecret().isBlank()) {
      log.error (sanitizerService.error ("JWT secret is not configured"));
      throw new IllegalStateException("JWT secret is not configured");
    }
    
    if (properties.getSecret().length() < MIN_SECRET_LENGTH) {
      log.error (sanitizerService.error ("JWT secret must be at least 32 characters long"));
      throw new IllegalStateException("JWT secret must be at least 32 characters long");
    }
    
    try {
      return Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    } catch (Exception ex) {
      log.error (sanitizerService.error ("Failed to initialize JWT signing key"), ex);
      throw new JwtException("Failed to initialize JWT signing key", ex);
    }
  }
  
  public String generateAccessToken(UserDetails userDetails) {
    validateUserDetails(userDetails);
    
    Map<String, Object> claims = new HashMap<>();
    claims.put("roles", userDetails.getAuthorities().stream()
                                   .map(GrantedAuthority::getAuthority)
                                   .collect(Collectors.toList()));
    
    try {
      return Jwts.builder()
                 .claims(claims)
                 .issuedAt(new Date())
                 .subject(userDetails.getUsername())
                 .expiration(new Date(System.currentTimeMillis() + properties.getAccessTokenExpiration()))
                 .signWith(signingKey)
                 .compact();
    } catch (Exception ex) {
      log.error (sanitizerService.error("Failed to generate access token"), ex);
      throw new JwtException("Failed to generate access token", ex);
    }
  }
  
  public String generateRefreshToken(UserDetails userDetails) {
    validateUserDetails(userDetails);
    
    try {
      return Jwts.builder()
                 .issuedAt(new Date())
                 .subject(userDetails.getUsername())
                 .expiration(new Date(System.currentTimeMillis() + properties.getRefreshTokenExpiration()))
                 .signWith(signingKey)
                 .compact();
    } catch (Exception ex) {
      log.error (sanitizerService.error("Failed to generate refresh token"), ex);
      throw new JwtException("Failed to generate refresh token", ex);
    }
  }
  
  private void validateUserDetails(UserDetails userDetails) {
    if (userDetails == null) {
      log.error (sanitizerService.error ("UserDetails cannot be null"));
      throw new IllegalArgumentException("UserDetails cannot be null");
    }
    if (userDetails.getUsername() == null || userDetails.getUsername().isBlank()) {
      log.error (sanitizerService.error ("Username cannot be empty"));
      throw new IllegalArgumentException("Username cannot be empty");
    }
  }
  
  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }
  
  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }
  
  public Date extractIssueDate(String token) {
    return extractClaim(token, Claims::getIssuedAt);
  }
  
  public boolean isTokenValid(String token) {
    try {
      extractAllClaims(token);
      return true;
    } catch (JwtException ex) {
      log.warn(sanitizerService.warning ("Invalid JWT token"),ex);
      return false;
    }
  }
  
  public boolean isTokenExpired(String token) {
    Date expiration = extractExpiration(token);
    return expiration.before(new Date());
  }
  
  public Authentication getAuthentication(String token) {
    try {
      String username = extractUsername(token);
      UserDetails userDetails = userDetailsService.loadUserByUsername(username);
      return new UsernamePasswordAuthenticationToken(
          userDetails,
          "",
          userDetails.getAuthorities()
      );
    } catch (UsernameNotFoundException ex) {
      log.error (sanitizerService.error ("User not found for JWT token"),ex);
      throw new JwtException("User not found", ex);
    }
  }
  
  private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    try {
      return Jwts.parser()
                 .verifyWith(signingKey)
                 .build()
                 .parseSignedClaims(token)
                 .getPayload();
    } catch (ExpiredJwtException e) {
      log.warn(sanitizerService.warning ("JWT token expired"),e);
      throw new JwtException("Token expired", e);
    } catch (UnsupportedJwtException e) {
      log.warn(sanitizerService.warning ("Unsupported JWT token: {}"), e);
      throw new JwtException("Unsupported token format", e);
    } catch (MalformedJwtException e) {
      log.warn(sanitizerService.warning ("Invalid JWT token format"), e);
      throw new JwtException("Invalid token format", e);
    } catch (SecurityException e) {
      log.warn(sanitizerService.warning ("JWT signature validation failed"), e);
      throw new JwtException("Invalid token signature", e);
    } catch (IllegalArgumentException e) {
      log.warn(sanitizerService.warning ("JWT claims string is empty"), e);
      throw new JwtException("Invalid token claims", e);
    }
  }
}