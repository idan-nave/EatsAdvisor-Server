package com.eatsadvisor.eatsadvisor.repositories;

import com.eatsadvisor.eatsadvisor.models.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DishRepository extends JpaRepository<Dish, Integer>, JpaSpecificationExecutor<Dish> {
    /**
     * Find a dish by its name
     * @param name The dish name to search for
     * @return An Optional containing the dish if found
     */
    Optional<Dish> findByName(String name);
    
    /**
     * Find dishes by name containing the given string (case insensitive)
     * @param name The name to search for
     * @return A list of dishes matching the search criteria
     */
    List<Dish> findByNameContainingIgnoreCase(String name);
    
    /**
     * Find dishes by description containing the given string (case insensitive)
     * @param description The description to search for
     * @return A list of dishes matching the search criteria
     */
    List<Dish> findByDescriptionContainingIgnoreCase(String description);
    
    /**
     * Check if a dish exists with the given name
     * @param name The dish name to check
     * @return true if a dish exists with the name, false otherwise
     */
    boolean existsByName(String name);
    
    /**
     * Find dishes that have been rated by a specific profile
     * @param profileId The ID of the profile
     * @return A list of dishes rated by the profile
     */
    @Query("SELECT dh.dish FROM DishHistory dh WHERE dh.profile.id = :profileId")
    List<Dish> findByProfileId(@Param("profileId") Integer profileId);
    
    /**
     * Find dishes that have been rated by a specific profile with a minimum rating
     * @param profileId The ID of the profile
     * @param minRating The minimum rating
     * @return A list of dishes rated by the profile with at least the specified rating
     */
    @Query("SELECT dh.dish FROM DishHistory dh WHERE dh.profile.id = :profileId AND dh.userRating >= :minRating")
    List<Dish> findByProfileIdAndMinRating(@Param("profileId") Integer profileId, @Param("minRating") Integer minRating);
    
    /**
     * Find the average rating for a specific dish
     * @param dishId The ID of the dish
     * @return The average rating for the dish
     */
    @Query("SELECT AVG(dh.userRating) FROM DishHistory dh WHERE dh.dish.id = :dishId")
    Double findAverageRatingByDishId(@Param("dishId") Integer dishId);
    
    /**
     * Find the most popular dishes (those with the highest average ratings)
     * @param limit The maximum number of dishes to return
     * @return A list of dishes with their average ratings
     */
    @Query("SELECT dh.dish, AVG(dh.userRating) as avgRating FROM DishHistory dh GROUP BY dh.dish ORDER BY avgRating DESC")
    List<Object[]> findMostPopularDishes(@Param("limit") Integer limit);
}
