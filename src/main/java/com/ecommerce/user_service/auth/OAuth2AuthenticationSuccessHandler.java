package com.ecommerce.user_service.auth;

import com.ecommerce.user_service.entity.UserEntity;
import com.ecommerce.user_service.enums.RoleEnum;
import com.ecommerce.user_service.service.JwtService;
import com.ecommerce.user_service.service.OAuthEmailService;
import com.ecommerce.user_service.service.RefreshTokenService;
import com.ecommerce.user_service.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component public class OAuth2AuthenticationSuccessHandler extends AbstractStatelessAuthenticationSuccessHandler
{
  @Autowired private OAuthEmailService oAuthEmailService;
  @Autowired private OAuth2AuthorizedClientService authorizedClientService;
  @Autowired private UserService userService;
  @Autowired private PasswordEncoder passwordEncoder;
  
  public OAuth2AuthenticationSuccessHandler (JwtService jwtService, RefreshTokenService refreshTokenService)
  {
    super (jwtService, refreshTokenService);
  }
  
  @Override public void onAuthenticationSuccess (HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws
                                                                                                                                          IOException,
                                                                                                                                          ServletException
  {
    OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
    UserEntity userEntity = registerUserOnTheFlyIfNotExist (extractUserEntity (authToken));
    UserDetails userDetails = User.builder ()
                                  .username (userEntity.getName ())
                                  .password (userEntity.getPassword ())
                                  .authorities (authToken.getPrincipal ().getAuthorities ())
                                  .build ();
    onSuccess (request, response, authentication, userDetails);
  }
  
  private UserEntity registerUserOnTheFlyIfNotExist (UserEntity userEntity)
  {
    Optional <UserEntity> existUserEntity = userService.findByEmail (userEntity.getEmail ());
    return existUserEntity.orElseGet (() -> userService.register (userEntity));
  }
  
  private UserEntity extractUserEntity (OAuth2AuthenticationToken authToken)
  {
    String extractedUserEmail = null;
    String extractedUserName = null;
    
    switch (authToken.getAuthorizedClientRegistrationId ())
    {
      case "google":
        extractedUserEmail = authToken.getPrincipal ().getAttribute ("email");
        extractedUserName = authToken.getPrincipal ().getAttribute ("name");
        break;
      case "github":
        extractedUserName = authToken.getPrincipal ().getAttribute ("login");
        String url = "https://api.github.com/user/emails";
        extractedUserEmail = oAuthEmailService.fetchPrimaryEmail (url, retrieveAccessToken (authToken));
        break;
    }
    if (extractedUserEmail == null)
      throw new RuntimeException ("Email address has not been provided in OAuth token!");
    
    return UserEntity.builder ()
                     .role (RoleEnum.USER.toString ())
                     .name (extractedUserName)
                     .email (extractedUserEmail)
                     .password (passwordEncoder.encode (UUID.randomUUID ().toString ()))
                     .build ();
  }
  
  private String retrieveAccessToken (OAuth2AuthenticationToken oAuth2AuthenticationToken)
  {
    String registrationId = oAuth2AuthenticationToken.getAuthorizedClientRegistrationId ();
    OAuth2AuthorizedClient client = this.authorizedClientService.loadAuthorizedClient (registrationId, oAuth2AuthenticationToken.getName ());
    
    if (client != null && client.getAccessToken () != null)
    {
      return client.getAccessToken ().getTokenValue ();
    }
    throw new RuntimeException ("Cannot retrieve access token from OAuth provider!");
  }
}
