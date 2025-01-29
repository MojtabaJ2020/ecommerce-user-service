package com.ecommerce.user_service.jwt;

import com.ecommerce.user_service.property.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Setter
@Component public class JwtUtil
{
  @Autowired
  private JwtProperties jwtProperties;
  @Autowired
  private UserDetailsService userDetailsService;
  
  public String generateAccessToken (UserDetails userDetails)
  {
    if(userDetails ==null)
    {
      throw new IllegalArgumentException ();
    }
    Map <String, Object> claims = new HashMap <> ();
    claims.put("role", userDetails.getAuthorities().stream()
                                  .map(GrantedAuthority::getAuthority)
                                  .collect(Collectors.toList()));
    Key key = Keys.hmacShaKeyFor (jwtProperties.getSecret ().getBytes (StandardCharsets.UTF_8));
    return Jwts.builder ()
               .claims (claims)
               .issuedAt (new Date ())
               .subject (userDetails.getUsername ())
               .expiration (new Date (System.currentTimeMillis () + jwtProperties.getAccessTokenExpiration ()))
               .signWith (key)
               .compact ();
  }
  
  public String generateRefreshToken (UserDetails serDetails )
  {
    Key key = Keys.hmacShaKeyFor (jwtProperties.getSecret ().getBytes (StandardCharsets.UTF_8));
    return Jwts.builder ()
               .issuedAt (new Date ())
               .subject (serDetails.getUsername ())
               .expiration (new Date (System.currentTimeMillis () + jwtProperties.getRefreshTokenExpiration ()))
               .signWith (key)
               .compact ();
  }
  
  public String extractUsername (String token)
  {
    return extractClaims (token, Claims::getSubject);
  }
  
  public Date extractExpiration (String token)
  {
    return extractClaims (token, Claims::getExpiration);
  }
  
  public Date extractIssueDate (String token)
  {
    return extractClaims (token, Claims::getIssuedAt);
  }
  
  private <T> T extractClaims (String token, Function <Claims, T> claimsResolver)
  {
    return claimsResolver.apply (extractAllClaims (token));
  }
  
  private Claims extractAllClaims (String token)
  {
    return Jwts.parser ().verifyWith (Keys.hmacShaKeyFor (jwtProperties.getSecret ().getBytes (StandardCharsets.UTF_8))).build ().parseSignedClaims (token).getPayload ();
  }
  
  public boolean isTokenExpired (String token)
  {
    return extractExpiration (token).after (new Date ());
  }
  
  public boolean validateToken(String token) {
    try {
      Claims claims = extractAllClaims(token);
      return true; // Token is valid
    } catch (Exception e) {
      return false; // Invalid token
    }
  }
  
  public Authentication getAuthentication(String token) {
    UserDetails userDetails = this.userDetailsService.loadUserByUsername(extractUsername (token));
    return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
  }
}
