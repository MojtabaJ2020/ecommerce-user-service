package com.ecommerce.user_service.service;

import com.ecommerce.user_service.constant.DefaultMessageSource;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class MailService
{
  private final MessageSource messageSource;
  private final JavaMailSender mailSender;
  
  public MailService (MessageSource messageSource, JavaMailSender mailSender)
  {
    this.messageSource = messageSource;
    this.mailSender = mailSender;
  }
  
  @Async
  public void sendUserActivationToken(String recipientEmail, String appBaseUrl, String token, Locale locale) {
    String subject = messageSource.getMessage("user.activation.token.email.subject", null, DefaultMessageSource.INFO_MESSAGE, locale);
    String confirmationUrl = appBaseUrl + "/confirm-registration?token=" + token;
    String message = messageSource.getMessage("user.activation.token.email.text", null, DefaultMessageSource.INFO_MESSAGE, locale);
    
    SimpleMailMessage email = new SimpleMailMessage();
    email.setTo(recipientEmail);
    email.setSubject(subject);
    email.setText(message + "\r\n" + confirmationUrl);
    mailSender.send (email);
  }
}
