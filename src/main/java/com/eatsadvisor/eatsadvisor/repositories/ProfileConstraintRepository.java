package com.eatsadvisor.eatsadvisor.repositories;

import com.eatsadvisor.eatsadvisor.models.ConstraintType;
import com.eatsadvisor.eatsadvisor.models.Profile;
import com.eatsadvisor.eatsadvisor.models.ProfileConstraint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileConstraintRepository extends JpaRepository<ProfileConstraint, Integer>, JpaSpecificationExecutor<ProfileConstraint> {
    /**
     * Find all profile constraints for a specific profile
     * @param profile The profile whose constraints should be retrieved
     * @return A list of profile constraints for the profile
     */
    List<ProfileConstraint> findByProfile(Profile profile);
    
    /**
     * Find all profile constraints for a specific constraint type
     * @param constraintType The constraint type whose profile associations should be retrieved
     * @return A list of profile constraints for the constraint type
     */
    List<ProfileConstraint> findByConstraintType(ConstraintType constraintType);
    
    /**
     * Find a specific profile constraint by profile and constraint type
     * @param profile The profile
     * @param constraintType The constraint type
     * @return An Optional containing the profile constraint if found
     */
    Optional<ProfileConstraint> findByProfileAndConstraintType(Profile profile, ConstraintType constraintType);
    
    /**
     * Find all profile constraints created after a specific time
     * @param createdAt The time after which constraints should be retrieved
     * @return A list of profile constraints created after the specified time
     */
    List<ProfileConstraint> findByCreatedAtAfter(Instant createdAt);
    
    /**
     * Check if a profile has a specific constraint type
     * @param profile The profile
     * @param constraintType The constraint type
     * @return true if the profile has the constraint type, false otherwise
     */
    boolean existsByProfileAndConstraintType(Profile profile, ConstraintType constraintType);
    
    /**
     * Delete a profile constraint by profile and constraint type
     * @param profile The profile
     * @param constraintType The constraint type
     */
    @Transactional
    void deleteByProfileAndConstraintType(Profile profile, ConstraintType constraintType);
    
    /**
     * Delete all profile constraints for a specific profile
     * @param profile The profile whose constraints should be deleted
     */
    @Transactional
    void deleteByProfile(Profile profile);
    
    /**
     * Count the number of profiles with a specific constraint type
     * @param constraintTypeId The ID of the constraint type
     * @return The number of profiles with the constraint type
     */
    @Query("SELECT COUNT(pc) FROM ProfileConstraint pc WHERE pc.constraintType.id = :constraintTypeId")
    long countByConstraintTypeId(@Param("constraintTypeId") Integer constraintTypeId);
    
    /**
     * Find profiles with a specific constraint type
     * @param constraintTypeId The ID of the constraint type
     * @return A list of profiles with the constraint type
     */
    @Query("SELECT pc.profile FROM ProfileConstraint pc WHERE pc.constraintType.id = :constraintTypeId")
    List<Profile> findProfilesByConstraintTypeId(@Param("constraintTypeId") Integer constraintTypeId);
}
