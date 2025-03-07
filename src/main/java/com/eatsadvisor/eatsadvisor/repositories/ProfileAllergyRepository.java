package com.eatsadvisor.eatsadvisor.repositories;

import com.eatsadvisor.eatsadvisor.models.Allergy;
import com.eatsadvisor.eatsadvisor.models.Profile;
import com.eatsadvisor.eatsadvisor.models.ProfileAllergy;
import com.eatsadvisor.eatsadvisor.models.ProfileAllergyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileAllergyRepository extends JpaRepository<ProfileAllergy, ProfileAllergyId>, JpaSpecificationExecutor<ProfileAllergy> {
    /**
     * Find all profile allergies for a specific profile
     * @param profile The profile whose allergies should be retrieved
     * @return A list of profile allergies for the profile
     */
    List<ProfileAllergy> findByProfile(Profile profile);
    
    /**
     * Find all profile allergies for a specific allergy
     * @param allergy The allergy whose profile associations should be retrieved
     * @return A list of profile allergies for the allergy
     */
    List<ProfileAllergy> findByAllergy(Allergy allergy);
    
    /**
     * Find a specific profile allergy by profile and allergy
     * @param profile The profile
     * @param allergy The allergy
     * @return An Optional containing the profile allergy if found
     */
    Optional<ProfileAllergy> findByProfileAndAllergy(Profile profile, Allergy allergy);
    
    /**
     * Check if a profile has a specific allergy
     * @param profile The profile
     * @param allergy The allergy
     * @return true if the profile has the allergy, false otherwise
     */
    boolean existsByProfileAndAllergy(Profile profile, Allergy allergy);
    
    /**
     * Delete a profile allergy by profile and allergy
     * @param profile The profile
     * @param allergy The allergy
     */
    @Transactional
    void deleteByProfileAndAllergy(Profile profile, Allergy allergy);
    
    /**
     * Delete all profile allergies for a specific profile
     * @param profile The profile whose allergies should be deleted
     */
    @Transactional
    void deleteByProfile(Profile profile);
    
    /**
     * Count the number of profiles with a specific allergy
     * @param allergyId The ID of the allergy
     * @return The number of profiles with the allergy
     */
    @Query("SELECT COUNT(pa) FROM ProfileAllergy pa WHERE pa.allergy.id = :allergyId")
    long countByAllergyId(@Param("allergyId") Integer allergyId);
}
