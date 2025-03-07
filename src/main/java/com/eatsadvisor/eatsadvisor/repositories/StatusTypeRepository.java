package com.eatsadvisor.eatsadvisor.repositories;

import com.eatsadvisor.eatsadvisor.models.StatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatusTypeRepository extends JpaRepository<StatusType, Integer>, JpaSpecificationExecutor<StatusType> {
    /**
     * Find a status type by its name
     * @param name The status type name to search for
     * @return An Optional containing the status type if found
     */
    Optional<StatusType> findByName(String name);
    
    /**
     * Check if a status type exists with the given name
     * @param name The status type name to check
     * @return true if a status type exists with the name, false otherwise
     */
    boolean existsByName(String name);
}
