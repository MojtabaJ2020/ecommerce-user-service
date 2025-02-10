package com.ecommerce.user_service.controller;

import com.ecommerce.user_service.constant.DefaultMessageSource;
import com.ecommerce.user_service.dto.ActivationTokenRequestDTO;
import com.ecommerce.user_service.dto.AuthRequestDTO;
import com.ecommerce.user_service.dto.UserDTO;
import com.ecommerce.user_service.entity.UserEntity;
import com.ecommerce.user_service.entity.VerificationToken;
import com.ecommerce.user_service.enums.TokenTypeEnum;
import com.ecommerce.user_service.event.OnRegistrationCompleteEvent;
import com.ecommerce.user_service.exception.InvalidRefreshTokenException;
import com.ecommerce.user_service.exception.UserInactiveException;
import com.ecommerce.user_service.model.ApiResponse;
import com.ecommerce.user_service.model.ApiSuccessResponse;
import com.ecommerce.user_service.model.CustomUserDetails;
import com.ecommerce.user_service.property.CommonProperties;
import com.ecommerce.user_service.property.RedirectUrlProperties;
import com.ecommerce.user_service.service.JwtService;
import com.ecommerce.user_service.service.LogSanitizerService;
import com.ecommerce.user_service.service.RefreshTokenService;
import com.ecommerce.user_service.service.UserService;
import com.ecommerce.user_service.service.VerificationTokenService;
import com.ecommerce.user_service.util.CookieUtil;
import com.ecommerce.user_service.util.DTOUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j @Controller public class AuthController
{
  private final AuthenticationManager authenticationManager;
  private final RefreshTokenService refreshTokenService;
  private final VerificationTokenService verificationTokenService;
  private final UserService userService;
  private final JwtService jwtService;
  private final MessageSource messageSource;
  private final ApplicationEventPublisher eventPublisher;
  private final CommonProperties commonProperties;
  private final RedirectUrlProperties redirectUrlProperties;
  private final LogSanitizerService sanitizerService;
  
  @Autowired
  public AuthController (AuthenticationManager authenticationManager,
                         RefreshTokenService refreshTokenService,
                         VerificationTokenService verificationTokenService,
                         UserService userService,
                         JwtService jwtService,
                         MessageSource messageSource,
                         ApplicationEventPublisher eventPublisher,
                         CommonProperties commonProperties,
                         RedirectUrlProperties redirectUrlProperties,
                         LogSanitizerService sanitizerService)
  {
    this.authenticationManager = authenticationManager;
    this.refreshTokenService = refreshTokenService;
    this.verificationTokenService = verificationTokenService;
    this.userService = userService;
    this.jwtService = jwtService;
    this.messageSource = messageSource;
    this.eventPublisher = eventPublisher;
    this.commonProperties = commonProperties;
    this.redirectUrlProperties = redirectUrlProperties;
    this.sanitizerService = sanitizerService;
  }
  
  @PostMapping ("/login")
  public ResponseEntity <?> login (@RequestBody @Valid AuthRequestDTO authRequestDTO, HttpServletRequest request, HttpServletResponse response)
  {
    Authentication authentication = authenticationManager.authenticate (new UsernamePasswordAuthenticationToken (authRequestDTO.getUsername (),
                                                                                                                 authRequestDTO.getPassword ()));
    
    CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal ();
    
    if (customUserDetails.getUserEntity ().isPendingActivation ())
    {
      throw new UserInactiveException ("User has not been activated yet.");
    }
    
    String accessToken = jwtService.generateAccessToken (customUserDetails);
    String refreshToken = jwtService.generateRefreshToken (customUserDetails);
    refreshTokenService.storeRefreshToken (request, customUserDetails, refreshToken);
    response.addCookie (CookieUtil.generate (accessToken, TokenTypeEnum.ACCESS_TOKEN, jwtService.getProperties ().getAccessTokenExpiration ()));
    response.addCookie (CookieUtil.generate (refreshToken, TokenTypeEnum.REFRESH_TOKEN, jwtService.getProperties ().getRefreshTokenExpiration ()));
    
    String message = messageSource.getMessage ("user.login.success", null, DefaultMessageSource.SUCCESS_MESSAGE, request.getLocale ());
    
    ApiResponse apiResponse = new ApiSuccessResponse<>  (message);
    
    log.info(sanitizerService.info (String.format ("User '%s' logged in successfully.", authRequestDTO.getUsername ())));
    
    return ResponseEntity.ok (apiResponse);
  }
  
  @PostMapping ("/refresh-token") public ResponseEntity <?> refreshToken (HttpServletRequest request, HttpServletResponse response)
  {
    Cookie[] cookies = request.getCookies ();
    String refreshToken = Arrays.stream (cookies)
                                .filter (cookie -> cookie.getName ().equals (TokenTypeEnum.REFRESH_TOKEN.toString ()))
                                .map (Cookie::getValue)
                                .findFirst ()
                                .orElse (null);
    
    if (refreshToken == null)
    {
      throw new InvalidRefreshTokenException ("Invalid refresh token");
    }
    
    String newAccessToken = this.refreshTokenService.refreshAccessToken (refreshToken);
    response.addCookie (CookieUtil.generate (newAccessToken, TokenTypeEnum.ACCESS_TOKEN, jwtService.getProperties ().getAccessTokenExpiration ()));
    
    log.info(sanitizerService.info ("Access token refreshed successfully."));
    
    ApiResponse apiResponse = new ApiSuccessResponse<>  ("Access token refreshed");
    
    return ResponseEntity.ok (apiResponse);
  }
  
  @PostMapping ("/register") public ResponseEntity <?> registerUser (HttpServletRequest request, @ModelAttribute @Valid UserDTO userDTO)
  {
    UserEntity userEntity = this.userService.register (DTOUtil.toEntity (userDTO));
    eventPublisher.publishEvent (new OnRegistrationCompleteEvent (userEntity, request.getLocale ()));
    
    String message = messageSource.getMessage ("user.registration.success", null, DefaultMessageSource.SUCCESS_MESSAGE, request.getLocale ());
    
    ApiResponse apiResponse = new ApiSuccessResponse <UserDTO> (message, List.of (DTOUtil.toDTO (userEntity)));
    
    log.info(sanitizerService.info (String.format ("User '%s' registered successfully.", userEntity.getName ())));
    
    return new ResponseEntity <> (apiResponse, HttpStatus.CREATED);
  }
  
  @PostMapping ("/send-activation-token")
  public ResponseEntity <?> sendActivationToken (HttpServletRequest request, @RequestBody @Valid ActivationTokenRequestDTO requestDTO)
  {
    Locale locale = request.getLocale ();
    String message = messageSource.getMessage ("user.activation.token.sent", null, DefaultMessageSource.SUCCESS_MESSAGE, locale);
    ApiResponse apiResponse = new ApiSuccessResponse<> (message);
    
    Optional <UserEntity> optionalUser = this.userService.findByEmail (requestDTO.getEmail ());
    if (optionalUser.isPresent () && optionalUser.get ().isPendingActivation ())
    {
      eventPublisher.publishEvent (new OnRegistrationCompleteEvent (optionalUser.get (), locale));
      log.info(sanitizerService.info (String.format ("Sent activation token for user '%s' successfully.",  optionalUser.get ().getName ())));
    }else
    {
      // this log need to be improved
      log.warn(sanitizerService.warning ("User not found or already active."));
    }
    
    //To avoid revealing existing emails, always sends success message
    return new ResponseEntity <> (apiResponse, HttpStatus.OK);
  }
  
  @GetMapping ("/confirm-registration")
  public RedirectView confirmRegistration (@RequestParam String token, HttpServletRequest request, RedirectAttributes redirectAttributes)
  {
    Locale locale = request.getLocale ();
    
    Optional <VerificationToken> optionalVerificationToken = verificationTokenService.findByToken (token);
    if (optionalVerificationToken.isEmpty ())
    {
      redirectAttributes.addFlashAttribute ("message",
                                            messageSource.getMessage ("user.activation.token.invalid",
                                                                      null,
                                                                      DefaultMessageSource.INVALID_MESSAGE,
                                                                      locale));
      return new RedirectView (commonProperties.getBaseUrl () + "/failed-user-activation");
    }
    
    VerificationToken verificationToken = optionalVerificationToken.get ();
    if (LocalDateTime.now ().isAfter (verificationToken.getExpiresAt ()))
    {
      redirectAttributes.addFlashAttribute ("message",
                                            messageSource.getMessage ("user.activation.token.expired",
                                                                      null,
                                                                      DefaultMessageSource.EXPIRED_MESSAGE,
                                                                      Locale.getDefault ()));
      return new RedirectView (commonProperties.getBaseUrl () + "/failed-user-activation");
    }
    
    UserEntity userEntity = verificationToken.getUserEntity ();
    userEntity.setPendingActivation (false);
    userService.update (userEntity);
    
    redirectAttributes.addFlashAttribute ("message",
                                          messageSource.getMessage ("user.activation.success", null, DefaultMessageSource.SUCCESS_MESSAGE, locale));
    return new RedirectView (commonProperties.getBaseUrl () + "/success-user-activation");
  }
  
  @GetMapping ("/failed-user-activation") public String failedActivationPage (Model model)
  {
    model.addAttribute ("send_activation_code_link", commonProperties.getFrontBaseUrl () + redirectUrlProperties.getAfterFailureActivation ());
    model.addAttribute ("login_link", commonProperties.getFrontBaseUrl () + redirectUrlProperties.getAfterSuccessActivation ());
    return "failed-user-activation";
  }
  
  @GetMapping ("/success-user-activation") public String successActivationPage (Model model)
  {
    model.addAttribute ("login_link", commonProperties.getFrontBaseUrl () + redirectUrlProperties.getAfterSuccessActivation ());
    return "success-user-activation";
  }
}
