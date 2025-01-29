package com.ecommerce.user_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode @Entity (name = "users") public class UserEntity
{
  @Id @GeneratedValue (strategy = GenerationType.SEQUENCE) private Long id;
  
  private String name;
  
  private String email;
  
  private String password;
  
  private String role;
  
  @Lob private byte[] picture;
  
  @OneToMany (mappedBy = "userEntity")
  private List <RefreshTokenEntity> refreshTokenEntityList;
  
}
