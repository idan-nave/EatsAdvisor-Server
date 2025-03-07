package com.eatsadvisor.eatsadvisor.repositories;

import com.eatsadvisor.eatsadvisor.models.Flavor;
import com.eatsadvisor.eatsadvisor.models.Profile;
import com.eatsadvisor.eatsadvisor.models.ProfileFlavorPreference;
import com.eatsadvisor.eatsadvisor.models.ProfileFlavorPreferenceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileFlavorPreferenceRepository extends JpaRepository<ProfileFlavorPreference, ProfileFlavorPreferenceId>, JpaSpecificationExecutor<ProfileFlavorPreference> {
    /**
     * Find all flavor preferences for a specific profile
     * @param profile The profile whose flavor preferences should be retrieved
     * @return A list of flavor preferences for the profile
     */
    List<ProfileFlavorPreference> findByProfile(Profile profile);
    
    /**
     * Find all flavor preferences for a specific flavor
     * @param flavor The flavor whose profile associations should be retrieved
     * @return A list of flavor preferences for the flavor
     */
    List<ProfileFlavorPreference> findByFlavor(Flavor flavor);
    
    /**
     * Find a specific flavor preference by profile and flavor
     * @param profile The profile
     * @param flavor The flavor
     * @return An Optional containing the flavor preference if found
     */
    Optional<ProfileFlavorPreference> findByProfileAndFlavor(Profile profile, Flavor flavor);
    
    /**
     * Find all flavor preferences for a profile with a minimum preference level
     * @param profile The profile
     * @param preferenceLevel The minimum preference level
     * @return A list of flavor preferences with at least the specified preference level
     */
    List<ProfileFlavorPreference> findByProfileAndPreferenceLevelGreaterThanEqual(Profile profile, Integer preferenceLevel);
    
    /**
     * Check if a profile has a preference for a specific flavor
     * @param profile The profile
     * @param flavor The flavor
     * @return true if the profile has a preference for the flavor, false otherwise
     */
    boolean existsByProfileAndFlavor(Profile profile, Flavor flavor);
    
    /**
     * Delete a flavor preference by profile and flavor
     * @param profile The profile
     * @param flavor The flavor
     */
    @Transactional
    void deleteByProfileAndFlavor(Profile profile, Flavor flavor);
    
    /**
     * Delete all flavor preferences for a specific profile
     * @param profile The profile whose flavor preferences should be deleted
     */
    @Transactional
    void deleteByProfile(Profile profile);
    
    /**
     * Find the average preference level for a specific flavor
     * @param flavorId The ID of the flavor
     * @return The average preference level for the flavor
     */
    @Query("SELECT AVG(pfp.preferenceLevel) FROM ProfileFlavorPreference pfp WHERE pfp.flavor.id = :flavorId")
    Double findAveragePreferenceLevelByFlavorId(@Param("flavorId") Integer flavorId);
    
    /**
     * Find the most popular flavors (those with the highest average preference levels)
     * @param limit The maximum number of flavors to return
     * @return A list of flavors with their average preference levels
     */
    @Query("SELECT pfp.flavor, AVG(pfp.preferenceLevel) as avgLevel FROM ProfileFlavorPreference pfp GROUP BY pfp.flavor ORDER BY avgLevel DESC")
    List<Object[]> findMostPopularFlavors(@Param("limit") Integer limit);
}
