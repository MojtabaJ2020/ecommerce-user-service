package com.ecommerce.user_service.repo;

import com.ecommerce.user_service.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository public interface RefreshTokenRepository extends JpaRepository <RefreshToken, Long>
{
  List <RefreshToken> findByToken (String token);
}
