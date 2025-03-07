package com.eatsadvisor.eatsadvisor.repositories;

import com.eatsadvisor.eatsadvisor.models.AppUser;
import com.eatsadvisor.eatsadvisor.models.AppUserStatus;
import com.eatsadvisor.eatsadvisor.models.StatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserStatusRepository extends JpaRepository<AppUserStatus, Integer>, JpaSpecificationExecutor<AppUserStatus> {
    /**
     * Find all statuses for a specific user
     * @param user The user whose statuses should be retrieved
     * @return A list of statuses for the user
     */
    List<AppUserStatus> findByUser(AppUser user);
    
    /**
     * Find all statuses for a specific status type
     * @param statusType The status type
     * @return A list of statuses for the status type
     */
    List<AppUserStatus> findByStatusType(StatusType statusType);
    
    /**
     * Find the latest status for a specific user
     * @param user The user whose latest status should be retrieved
     * @return An Optional containing the latest status if found
     */
    @Query("SELECT s FROM AppUserStatus s WHERE s.user = :user ORDER BY s.createdAt DESC")
    List<AppUserStatus> findLatestByUser(@Param("user") AppUser user);
    
    /**
     * Find all statuses created after a specific time
     * @param createdAt The time after which statuses should be retrieved
     * @return A list of statuses created after the specified time
     */
    List<AppUserStatus> findByCreatedAtAfter(Instant createdAt);
    
    /**
     * Find a specific status for a user and status type
     * @param user The user
     * @param statusType The status type
     * @return An Optional containing the status if found
     */
    Optional<AppUserStatus> findByUserAndStatusType(AppUser user, StatusType statusType);
    
    /**
     * Check if a user has a specific status type
     * @param user The user
     * @param statusType The status type
     * @return true if the user has the status type, false otherwise
     */
    boolean existsByUserAndStatusType(AppUser user, StatusType statusType);
    
    /**
     * Find all users with a specific status type
     * @param statusTypeName The name of the status type
     * @return A list of users with the status type
     */
    @Query("SELECT s.user FROM AppUserStatus s WHERE s.statusType.name = :statusTypeName")
    List<AppUser> findUsersByStatusTypeName(@Param("statusTypeName") String statusTypeName);
}
