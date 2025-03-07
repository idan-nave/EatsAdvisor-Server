package com.eatsadvisor.eatsadvisor.repositories;

import com.eatsadvisor.eatsadvisor.models.AppRole;
import com.eatsadvisor.eatsadvisor.models.AppUser;
import com.eatsadvisor.eatsadvisor.models.AppUserRoleMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRoleMappingRepository extends JpaRepository<AppUserRoleMapping, Integer>, JpaSpecificationExecutor<AppUserRoleMapping> {
    /**
     * Find all role mappings for a specific user
     * @param user The user whose role mappings should be retrieved
     * @return A list of role mappings for the user
     */
    List<AppUserRoleMapping> findByUser(AppUser user);
    
    /**
     * Find all active role mappings for a specific user
     * @param user The user whose role mappings should be retrieved
     * @param isActive Whether the role mapping is active
     * @return A list of active role mappings for the user
     */
    List<AppUserRoleMapping> findByUserAndIsActive(AppUser user, boolean isActive);
    
    /**
     * Find all role mappings for a specific role
     * @param role The role whose mappings should be retrieved
     * @return A list of role mappings for the role
     */
    List<AppUserRoleMapping> findByRole(AppRole role);
    
    /**
     * Find a specific role mapping for a user and role
     * @param user The user
     * @param role The role
     * @return An Optional containing the role mapping if found
     */
    Optional<AppUserRoleMapping> findByUserAndRole(AppUser user, AppRole role);
    
    /**
     * Find all users with a specific role
     * @param roleName The name of the role
     * @return A list of users with the role
     */
    @Query("SELECT m.user FROM AppUserRoleMapping m WHERE m.role.name = :roleName AND m.isActive = true")
    List<AppUser> findUsersByRoleName(@Param("roleName") String roleName);
    
    /**
     * Check if a user has a specific role
     * @param user The user
     * @param role The role
     * @return true if the user has the role, false otherwise
     */
    boolean existsByUserAndRole(AppUser user, AppRole role);
    
    /**
     * Check if a user has a specific active role
     * @param user The user
     * @param role The role
     * @param isActive Whether the role mapping is active
     * @return true if the user has the active role, false otherwise
     */
    boolean existsByUserAndRoleAndIsActive(AppUser user, AppRole role, boolean isActive);
}
