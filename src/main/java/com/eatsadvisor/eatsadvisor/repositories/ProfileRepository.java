package com.eatsadvisor.eatsadvisor.repositories;

import com.eatsadvisor.eatsadvisor.models.AppUser;
import com.eatsadvisor.eatsadvisor.models.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Integer>, JpaSpecificationExecutor<Profile> {
    /**
     * Find a profile by its associated user
     * @param user The user whose profile should be retrieved
     * @return An Optional containing the profile if found
     */
    Optional<Profile> findByUser(AppUser user);
    
    /**
     * Find a profile by the user's ID
     * @param userId The ID of the user whose profile should be retrieved
     * @return An Optional containing the profile if found
     */
    @Query("SELECT p FROM Profile p WHERE p.user.id = :userId")
    Optional<Profile> findByUserId(@Param("userId") Integer userId);
    
    /**
     * Find all profiles created after a specific time
     * @param createdAt The time after which profiles should be retrieved
     * @return A list of profiles created after the specified time
     */
    List<Profile> findByCreatedAtAfter(Instant createdAt);
    
    /**
     * Check if a profile exists for a specific user
     * @param user The user to check
     * @return true if a profile exists for the user, false otherwise
     */
    boolean existsByUser(AppUser user);
    
    /**
     * Find profiles with specific allergies
     * @param allergyId The ID of the allergy
     * @return A list of profiles with the allergy
     */
    @Query("SELECT pa.profile FROM ProfileAllergy pa WHERE pa.allergy.id = :allergyId")
    List<Profile> findByAllergyId(@Param("allergyId") Integer allergyId);
    
    /**
     * Find profiles with specific flavor preferences
     * @param flavorId The ID of the flavor
     * @param minPreferenceLevel The minimum preference level
     * @return A list of profiles with the flavor preference
     */
    @Query("SELECT pfp.profile FROM ProfileFlavorPreference pfp WHERE pfp.flavor.id = :flavorId AND pfp.preferenceLevel >= :minPreferenceLevel")
    List<Profile> findByFlavorPreference(@Param("flavorId") Integer flavorId, @Param("minPreferenceLevel") Integer minPreferenceLevel);
}
