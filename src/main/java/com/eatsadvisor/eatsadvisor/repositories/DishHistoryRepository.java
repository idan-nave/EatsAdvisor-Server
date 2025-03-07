package com.eatsadvisor.eatsadvisor.repositories;

import com.eatsadvisor.eatsadvisor.models.Dish;
import com.eatsadvisor.eatsadvisor.models.DishHistory;
import com.eatsadvisor.eatsadvisor.models.Profile;
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
public interface DishHistoryRepository extends JpaRepository<DishHistory, Integer>, JpaSpecificationExecutor<DishHistory> {
    /**
     * Find all dish history entries for a specific profile
     * @param profile The profile whose dish history should be retrieved
     * @return A list of dish history entries for the profile
     */
    List<DishHistory> findByProfile(Profile profile);
    
    /**
     * Find all dish history entries for a specific dish
     * @param dish The dish whose history should be retrieved
     * @return A list of dish history entries for the dish
     */
    List<DishHistory> findByDish(Dish dish);
    
    /**
     * Find a specific dish history entry by profile and dish
     * @param profile The profile
     * @param dish The dish
     * @return An Optional containing the dish history entry if found
     */
    Optional<DishHistory> findByProfileAndDish(Profile profile, Dish dish);
    
    /**
     * Find all dish history entries for a profile with a minimum rating
     * @param profile The profile
     * @param userRating The minimum rating
     * @return A list of dish history entries with at least the specified rating
     */
    List<DishHistory> findByProfileAndUserRatingGreaterThanEqual(Profile profile, Integer userRating);
    
    /**
     * Find all dish history entries created after a specific time
     * @param createdAt The time after which entries should be retrieved
     * @return A list of dish history entries created after the specified time
     */
    List<DishHistory> findByCreatedAtAfter(Instant createdAt);
    
    /**
     * Check if a profile has rated a specific dish
     * @param profile The profile
     * @param dish The dish
     * @return true if the profile has rated the dish, false otherwise
     */
    boolean existsByProfileAndDish(Profile profile, Dish dish);
    
    /**
     * Delete a dish history entry by profile and dish
     * @param profile The profile
     * @param dish The dish
     */
    @Transactional
    void deleteByProfileAndDish(Profile profile, Dish dish);
    
    /**
     * Delete all dish history entries for a specific profile
     * @param profile The profile whose dish history should be deleted
     */
    @Transactional
    void deleteByProfile(Profile profile);
    
    /**
     * Find the most recent dish history entries for a specific profile
     * @param profileId The ID of the profile
     * @param limit The maximum number of entries to return
     * @return A list of the most recent dish history entries for the profile
     */
    @Query("SELECT dh FROM DishHistory dh WHERE dh.profile.id = :profileId ORDER BY dh.createdAt DESC")
    List<DishHistory> findMostRecentByProfileId(@Param("profileId") Integer profileId, @Param("limit") Integer limit);
    
    /**
     * Find the average rating for dishes rated by a specific profile
     * @param profileId The ID of the profile
     * @return The average rating for dishes rated by the profile
     */
    @Query("SELECT AVG(dh.userRating) FROM DishHistory dh WHERE dh.profile.id = :profileId")
    Double findAverageRatingByProfileId(@Param("profileId") Integer profileId);
}
