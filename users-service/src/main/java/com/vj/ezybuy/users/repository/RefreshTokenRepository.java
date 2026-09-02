package com.vj.ezybuy.users.repository;

import com.vj.ezybuy.users.entity.RefreshToken;
import com.vj.ezybuy.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    Optional<RefreshToken> findByRefreshToken(String refreshToken);
    Optional<RefreshToken> findByRefreshTokenAndUserId(String refreshToken, String userId);
    Optional<RefreshToken> findByUser(User user);

}
