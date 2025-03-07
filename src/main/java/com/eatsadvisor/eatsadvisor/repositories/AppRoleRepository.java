package com.eatsadvisor.eatsadvisor.repositories;

import com.eatsadvisor.eatsadvisor.models.AppRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppRoleRepository extends JpaRepository<AppRole, Integer>, JpaSpecificationExecutor<AppRole> {
    /**
     * Find a role by its name
     * @param name The role name to search for
     * @return An Optional containing the role if found
     */
    Optional<AppRole> findByName(String name);
    
    /**
     * Check if a role exists with the given name
     * @param name The role name to check
     * @return true if a role exists with the name, false otherwise
     */
    boolean existsByName(String name);
}
