package com.ecommerce.user_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Entity
@Table (name = "refresh_token")
public class RefreshToken
{
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String provider;
    
    private String token;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime expiresAt;
    
    private boolean revoked;

    private LocalDateTime lastUsedAt;

    private String ipAddress;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;
}
