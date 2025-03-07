package com.eatsadvisor.eatsadvisor.repositories;

import com.eatsadvisor.eatsadvisor.models.Flavor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlavorRepository extends JpaRepository<Flavor, Integer>, JpaSpecificationExecutor<Flavor> {
    /**
     * Find a flavor by its name
     * @param name The flavor name to search for
     * @return An Optional containing the flavor if found
     */
    Optional<Flavor> findByName(String name);
    
    /**
     * Find flavors by name containing the given string (case insensitive)
     * @param name The name to search for
     * @return A list of flavors matching the search criteria
     */
    List<Flavor> findByNameContainingIgnoreCase(String name);
    
    /**
     * Check if a flavor exists with the given name
     * @param name The flavor name to check
     * @return true if a flavor exists with the name, false otherwise
     */
    boolean existsByName(String name);
    
    /**
     * Find flavors associated with a specific profile
     * @param profileId The ID of the profile
     * @return A list of flavors associated with the profile
     */
    @Query("SELECT pfp.flavor FROM ProfileFlavorPreference pfp WHERE pfp.profile.id = :profileId")
    List<Flavor> findByProfileId(@Param("profileId") Integer profileId);
    
    /**
     * Find flavors associated with a specific profile with a minimum preference level
     * @param profileId The ID of the profile
     * @param minPreferenceLevel The minimum preference level
     * @return A list of flavors associated with the profile with the minimum preference level
     */
    @Query("SELECT pfp.flavor FROM ProfileFlavorPreference pfp WHERE pfp.profile.id = :profileId AND pfp.preferenceLevel >= :minPreferenceLevel")
    List<Flavor> findByProfileIdAndMinPreferenceLevel(@Param("profileId") Integer profileId, @Param("minPreferenceLevel") Integer minPreferenceLevel);
}
