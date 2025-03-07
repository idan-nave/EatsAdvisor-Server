package com.eatsadvisor.eatsadvisor.repositories;

import com.eatsadvisor.eatsadvisor.models.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long>, JpaSpecificationExecutor<AppUser> {
    /**
     * Find a user by their email address
     * @param email The email to search for
     * @return An Optional containing the user if found
     */
    Optional<AppUser> findByEmail(String email);
    
    /**
     * Find a user by their OAuth provider ID
     * @param oauthProviderId The OAuth provider ID
     * @return An Optional containing the user if found
     */
    Optional<AppUser> findByOauthProviderId(String oauthProviderId);
    
    /**
     * Find a user by their OAuth provider and provider ID
     * @param oauthProvider The OAuth provider (e.g., "google", "github")
     * @param oauthProviderId The OAuth provider ID
     * @return An Optional containing the user if found
     */
    Optional<AppUser> findByOauthProviderAndOauthProviderId(String oauthProvider, String oauthProviderId);
    
    /**
     * Find a user by their username
     * @param username The username to search for
     * @return An Optional containing the user if found
     */
    Optional<AppUser> findByUsername(String username);
    
    /**
     * Check if a user exists with the given email
     * @param email The email to check
     * @return true if a user exists with the email, false otherwise
     */
    boolean existsByEmail(String email);
    
    /**
     * Check if a user exists with the given OAuth provider ID
     * @param oauthProviderId The OAuth provider ID to check
     * @return true if a user exists with the OAuth provider ID, false otherwise
     */
    boolean existsByOauthProviderId(String oauthProviderId);
    
    /**
     * Find users by first name or last name containing the given string (case insensitive)
     * @param name The name to search for
     * @return A list of users matching the search criteria
     */
    @Query("SELECT u FROM AppUser u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<AppUser> findByNameContainingIgnoreCase(@Param("name") String name);
}
