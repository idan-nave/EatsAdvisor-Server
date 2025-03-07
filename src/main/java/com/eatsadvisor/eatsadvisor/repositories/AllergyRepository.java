package com.eatsadvisor.eatsadvisor.repositories;

import com.eatsadvisor.eatsadvisor.models.Allergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AllergyRepository extends JpaRepository<Allergy, Integer>, JpaSpecificationExecutor<Allergy> {
    /**
     * Find an allergy by its name
     * @param name The allergy name to search for
     * @return An Optional containing the allergy if found
     */
    Optional<Allergy> findByName(String name);
    
    /**
     * Find allergies by name containing the given string (case insensitive)
     * @param name The name to search for
     * @return A list of allergies matching the search criteria
     */
    List<Allergy> findByNameContainingIgnoreCase(String name);
    
    /**
     * Check if an allergy exists with the given name
     * @param name The allergy name to check
     * @return true if an allergy exists with the name, false otherwise
     */
    boolean existsByName(String name);
    
    /**
     * Find allergies associated with a specific profile
     * @param profileId The ID of the profile
     * @return A list of allergies associated with the profile
     */
    @Query("SELECT pa.allergy FROM ProfileAllergy pa WHERE pa.profile.id = :profileId")
    List<Allergy> findByProfileId(@Param("profileId") Integer profileId);
}
