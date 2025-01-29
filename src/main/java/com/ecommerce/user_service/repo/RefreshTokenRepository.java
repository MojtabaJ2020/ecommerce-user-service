package com.ecommerce.user_service.repo;

import com.ecommerce.user_service.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository public interface RefreshTokenRepository extends JpaRepository <RefreshTokenEntity, Long>
{
  List <RefreshTokenEntity> findByToken (String token);
}
