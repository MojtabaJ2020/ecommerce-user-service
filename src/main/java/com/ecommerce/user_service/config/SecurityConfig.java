package com.ecommerce.user_service.config;

import com.ecommerce.user_service.enums.TokenTypeEnum;
import com.ecommerce.user_service.jwt.JwtAuthenticationEntryPoint;
import com.ecommerce.user_service.jwt.JwtAuthenticationFilter;
import com.ecommerce.user_service.jwt.JwtUtil;
import com.ecommerce.user_service.oauth.OAuth2AuthenticationSuccessHandler;
import com.ecommerce.user_service.property.JwtProperties;
import com.ecommerce.user_service.service.RefreshTokenService;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Configuration @EnableMethodSecurity public class SecurityConfig
{
  private final UserDetailsService userDetailsService;
  private final RefreshTokenService refreshTokenService;
  private final JwtUtil jwtUtil;
  private final JwtProperties jwtProperties;
  
  public SecurityConfig (UserDetailsService userDetailsService, RefreshTokenService refreshTokenService, JwtUtil jwtUtil, JwtProperties jwtProperties)
  {
    this.userDetailsService = userDetailsService;
    this.refreshTokenService = refreshTokenService;
    this.jwtUtil = jwtUtil;
    this.jwtProperties = jwtProperties;
  }
  
  @Bean public SecurityFilterChain securityFilterChain (HttpSecurity http) throws Exception
  {
    http.csrf (AbstractHttpConfigurer::disable)
        .authorizeHttpRequests (auth -> auth.requestMatchers ("/api/**").authenticated ().anyRequest ().permitAll ())
        
        .oauth2Login (oauth2 -> oauth2.loginPage ("/login")  // Use custom login page
                                      .defaultSuccessUrl ("/home", true).successHandler (oAuth2AuthenticationSuccessHandler ()))
        
        .httpBasic (Customizer.withDefaults ())
        .formLogin (form -> form.loginPage ("/login").successHandler (standardAuthenticationSuccessHandler ()))
        .authenticationProvider (authenticationProvider ())
        .addFilterBefore (jwtRequestFilter (), UsernamePasswordAuthenticationFilter.class)
        //        .oauth2ResourceServer (oauth2 -> oauth2.jwt (Customizer.withDefaults ()))
        .sessionManagement (session -> session.sessionCreationPolicy (SessionCreationPolicy.STATELESS))
        .exceptionHandling (exceptionHandling -> exceptionHandling.authenticationEntryPoint (jwtAuthenticationEntryPoint ()))
        .logout ((logout) -> logout.deleteCookies (TokenTypeEnum.ACCESS_TOKEN.toString ())
                                   .invalidateHttpSession (true)
                                   .logoutUrl ("/logout")
                                   .logoutSuccessUrl ("/login"));
    return http.build ();
  }
  
  @Bean public JwtDecoder jwtDecoder ()
  {
    SecretKey key = Keys.hmacShaKeyFor (jwtProperties.getSecret ().getBytes (StandardCharsets.UTF_8));
    return NimbusJwtDecoder.withSecretKey (key).build ();
  }
  
  @Bean public PasswordEncoder passwordEncoder ()
  {
    return new BCryptPasswordEncoder ();
  }
  
  @Bean public StandardAuthenticationSuccessHandler standardAuthenticationSuccessHandler ()
  {
    return new StandardAuthenticationSuccessHandler (jwtUtil, refreshTokenService);
  }
  
  @Bean public OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler ()
  {
    return new OAuth2AuthenticationSuccessHandler (jwtUtil, refreshTokenService);
  }
  
  @Bean public AuthenticationProvider authenticationProvider ()
  {
    DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider (passwordEncoder ());
    authenticationProvider.setUserDetailsService (userDetailsService);
    authenticationProvider.setAuthoritiesMapper (new SimpleAuthorityMapper ());
    return authenticationProvider;
  }
  
  @Bean public AuthenticationManager authenticationManager (AuthenticationConfiguration authenticationConfiguration) throws Exception
  {
    return authenticationConfiguration.getAuthenticationManager ();
  }
  
  @Bean public JwtAuthenticationFilter jwtRequestFilter ()
  {
    return new JwtAuthenticationFilter (userDetailsService, jwtUtil);
  }
  
  @Bean public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint ()
  {
    return new JwtAuthenticationEntryPoint ();
  }
  
}