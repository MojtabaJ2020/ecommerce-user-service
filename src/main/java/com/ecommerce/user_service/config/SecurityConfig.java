package com.ecommerce.user_service.config;

import com.ecommerce.user_service.auth.OAuth2AuthenticationSuccessHandler;
import com.ecommerce.user_service.auth.StandardAuthenticationSuccessHandler;
import com.ecommerce.user_service.enums.TokenTypeEnum;
import com.ecommerce.user_service.jwt.JwtAuthenticationEntryPoint;
import com.ecommerce.user_service.jwt.JwtAuthenticationFilter;
import com.ecommerce.user_service.service.JwtService;
import com.ecommerce.user_service.property.JwtProperties;
import com.ecommerce.user_service.property.RedirectUrlProperties;
import com.ecommerce.user_service.property.SecurityProperties;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Configuration @EnableMethodSecurity public class SecurityConfig
{
  private final UserDetailsService userDetailsService;
  private final RefreshTokenService refreshTokenService;
  private final JwtService jwtService;
  private final JwtProperties jwtProperties;
  private final SecurityProperties securityProperties;
  private final RedirectUrlProperties redirectUrlProperties;
  private final PasswordEncoder passwordEncoder;
  
  public SecurityConfig (UserDetailsService userDetailsService,
                         RefreshTokenService refreshTokenService,
                         JwtService jwtService,
                         JwtProperties jwtProperties,
                         SecurityProperties securityProperties,
                         RedirectUrlProperties redirectUrlProperties,
                         PasswordEncoder passwordEncoder)
  {
    this.userDetailsService = userDetailsService;
    this.refreshTokenService = refreshTokenService;
    this.jwtService = jwtService;
    this.jwtProperties = jwtProperties;
    this.securityProperties = securityProperties;
    this.redirectUrlProperties = redirectUrlProperties;
    this.passwordEncoder = passwordEncoder;
  }
  
  @Bean public SecurityFilterChain securityFilterChain (HttpSecurity http) throws Exception
  {
    http.csrf (AbstractHttpConfigurer::disable)
        .authorizeHttpRequests (auth -> auth.requestMatchers (securityProperties.getWhitelistedEndpoints ().toArray (new String[0]))
                                            .permitAll ()
                                            .anyRequest ()
                                            .authenticated ())
        
        // add oauth2.loginPage ("/login-basic") for using backend custom login page
        .oauth2Login (oauth2 -> oauth2.successHandler (oAuth2AuthenticationSuccessHandler ()).failureHandler (authenticationFailureHandler ()))
        
        .httpBasic (Customizer.withDefaults ())
        
        // Enable following line for using backend custom login page
        //        .formLogin (form -> form.loginPage ("/login-basic")
        //                                .successHandler (standardAuthenticationSuccessHandler ())
        //                                .failureHandler (authenticationFailureHandler ()))
        
        .authenticationProvider (authenticationProvider ())
        .addFilterBefore (jwtRequestFilter (), UsernamePasswordAuthenticationFilter.class)
        .sessionManagement (session -> session.sessionCreationPolicy (SessionCreationPolicy.STATELESS))
        .exceptionHandling (exceptionHandling -> exceptionHandling.authenticationEntryPoint (jwtAuthenticationEntryPoint ()))
        .logout ((logout) -> logout.deleteCookies (TokenTypeEnum.ACCESS_TOKEN.toString (),TokenTypeEnum.REFRESH_TOKEN.toString ())
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
  
  @Bean public StandardAuthenticationSuccessHandler standardAuthenticationSuccessHandler ()
  {
    StandardAuthenticationSuccessHandler successHandler = new StandardAuthenticationSuccessHandler (jwtService, refreshTokenService);
    successHandler.setDefaultTargetUrl (redirectUrlProperties.getAfterSuccessLogin ());
    return successHandler;
  }
  
  @Bean public OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler ()
  {
    OAuth2AuthenticationSuccessHandler successHandler = new OAuth2AuthenticationSuccessHandler (jwtService, refreshTokenService);
    successHandler.setDefaultTargetUrl (redirectUrlProperties.getAfterSuccessLogin ());
    return successHandler;
  }
  
  @Bean public SimpleUrlAuthenticationFailureHandler authenticationFailureHandler ()
  {
    return new SimpleUrlAuthenticationFailureHandler (redirectUrlProperties.getAfterFailureLogin ());
  }
  
  @Bean public AuthenticationProvider authenticationProvider ()
  {
    DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider (passwordEncoder);
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
    return new JwtAuthenticationFilter (userDetailsService, jwtService);
  }
  
  @Bean public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint ()
  {
    return new JwtAuthenticationEntryPoint ();
  }
  
}