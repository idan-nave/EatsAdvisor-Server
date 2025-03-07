package com.eatsadvisor.eatsadvisor.repositories;

import com.eatsadvisor.eatsadvisor.models.ConstraintType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConstraintTypeRepository extends JpaRepository<ConstraintType, Integer>, JpaSpecificationExecutor<ConstraintType> {
    /**
     * Find a constraint type by its name
     * @param name The constraint type name to search for
     * @return An Optional containing the constraint type if found
     */
    Optional<ConstraintType> findByName(String name);
    
    /**
     * Find constraint types by name containing the given string (case insensitive)
     * @param name The name to search for
     * @return A list of constraint types matching the search criteria
     */
    List<ConstraintType> findByNameContainingIgnoreCase(String name);
    
    /**
     * Check if a constraint type exists with the given name
     * @param name The constraint type name to check
     * @return true if a constraint type exists with the name, false otherwise
     */
    boolean existsByName(String name);
    
    /**
     * Find constraint types associated with a specific profile
     * @param profileId The ID of the profile
     * @return A list of constraint types associated with the profile
     */
    @Query("SELECT pc.constraintType FROM ProfileConstraint pc WHERE pc.profile.id = :profileId")
    List<ConstraintType> findByProfileId(@Param("profileId") Integer profileId);
    
    /**
     * Count the number of profiles with a specific constraint type
     * @param constraintTypeId The ID of the constraint type
     * @return The number of profiles with the constraint type
     */
    @Query("SELECT COUNT(pc) FROM ProfileConstraint pc WHERE pc.constraintType.id = :constraintTypeId")
    long countByConstraintTypeId(@Param("constraintTypeId") Integer constraintTypeId);
}
