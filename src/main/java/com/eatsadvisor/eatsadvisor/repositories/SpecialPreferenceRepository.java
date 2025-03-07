package com.eatsadvisor.eatsadvisor.repositories;

import com.eatsadvisor.eatsadvisor.models.Profile;
import com.eatsadvisor.eatsadvisor.models.SpecialPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Repository
public interface SpecialPreferenceRepository extends JpaRepository<SpecialPreference, Integer>, JpaSpecificationExecutor<SpecialPreference> {
    /**
     * Find all special preferences for a specific profile
     * @param profile The profile whose special preferences should be retrieved
     * @return A list of special preferences for the profile
     */
    List<SpecialPreference> findByProfile(Profile profile);
    
    /**
     * Find all special preferences for a specific profile ID
     * @param profileId The ID of the profile whose special preferences should be retrieved
     * @return A list of special preferences for the profile
     */
    List<SpecialPreference> findByProfileId(Integer profileId);
    
    /**
     * Find all special preferences containing the given description (case insensitive)
     * @param description The description to search for
     * @return A list of special preferences matching the search criteria
     */
    List<SpecialPreference> findByDescriptionContainingIgnoreCase(String description);
    
    /**
     * Find all special preferences created after a specific time
     * @param createdAt The time after which preferences should be retrieved
     * @return A list of special preferences created after the specified time
     */
    List<SpecialPreference> findByCreatedAtAfter(Instant createdAt);
    
    /**
     * Delete all special preferences for a specific profile
     * @param profile The profile whose special preferences should be deleted
     */
    @Transactional
    void deleteByProfile(Profile profile);
    
    /**
     * Count the number of special preferences for a specific profile
     * @param profileId The ID of the profile
     * @return The number of special preferences for the profile
     */
    @Query("SELECT COUNT(sp) FROM SpecialPreference sp WHERE sp.profile.id = :profileId")
    long countByProfileId(@Param("profileId") Integer profileId);
    
    /**
     * Find special preferences with descriptions matching a keyword
     * @param keyword The keyword to search for
     * @return A list of special preferences with descriptions matching the keyword
     */
    @Query("SELECT sp FROM SpecialPreference sp WHERE LOWER(sp.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<SpecialPreference> findByKeyword(@Param("keyword") String keyword);
}
