package com.ecommerce.user_service.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
  
  private boolean enabled;
  
  private boolean locked;
  
  private boolean pendingActivation;
  
  private String role;
  
  @Basic(fetch = FetchType.LAZY)
  @Column (columnDefinition = "BYTEA")
  private byte[] picture;
  
  @OneToMany (mappedBy = "userEntity")
  private List <RefreshToken> refreshTokenList;
  
}
