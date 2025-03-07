package com.eatsadvisor.eatsadvisor.repositories;

import com.eatsadvisor.eatsadvisor.models.RefreshToken;
import com.eatsadvisor.eatsadvisor.models.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    /**
     * Find a refresh token by its token value
     * @param token The token value to search for
     * @return An Optional containing the refresh token if found
     */
    Optional<RefreshToken> findByToken(String token);
    
    /**
     * Delete all refresh tokens for a specific user
     * @param user The user whose tokens should be deleted
     */
    @Transactional
    void deleteByUser(AppUser user);
    
    /**
     * Find all refresh tokens for a specific user
     * @param user The user whose tokens should be retrieved
     * @return A list of refresh tokens for the user
     */
    List<RefreshToken> findByUser(AppUser user);
    
    /**
     * Find all expired refresh tokens
     * @param now The current time
     * @return A list of expired refresh tokens
     */
    List<RefreshToken> findByExpiryBefore(Instant now);
    
    /**
     * Delete all expired refresh tokens
     * @param now The current time
     * @return The number of tokens deleted
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken r WHERE r.expiry < :now")
    int deleteAllExpiredTokens(@Param("now") Instant now);
    
    /**
     * Check if a refresh token exists
     * @param token The token value to check
     * @return true if the token exists, false otherwise
     */
    boolean existsByToken(String token);
}
