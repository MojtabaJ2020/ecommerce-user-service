package com.ecommerce.user_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode @Entity (name = "verification_token") public class VerificationToken
{
  @Id @GeneratedValue (strategy = GenerationType.SEQUENCE) private Long id;
  
  private String token;
  
  private LocalDateTime expiresAt;

  @ManyToOne
  @JoinColumn (name = "user_id")
  private UserEntity userEntity;
  
}
