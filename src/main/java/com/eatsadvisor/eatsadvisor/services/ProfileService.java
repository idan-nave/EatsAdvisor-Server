package com.eatsadvisor.eatsadvisor.services;

import com.eatsadvisor.eatsadvisor.models.*;
import com.eatsadvisor.eatsadvisor.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ProfileService {
    private static final Logger logger = LoggerFactory.getLogger(ProfileService.class);
    private final ProfileRepository profileRepository;
    private final AppUserRepository appUserRepository;
    private final RecommendationService recommendationService;

    public ProfileService(
            ProfileRepository profileRepository,
            AppUserRepository appUserRepository,
            RecommendationService recommendationService) {
        this.profileRepository = profileRepository;
        this.appUserRepository = appUserRepository;
        this.recommendationService = recommendationService;
    }

    /**
     * Get all profiles
     * @return List of all profiles
     */
    public List<Profile> getAllProfiles() {
        return profileRepository.findAll();
    }

    /**
     * Get a profile by ID
     * @param id The profile ID
     * @return The profile if found
     */
    public Optional<Profile> getProfileById(Integer id) {
        return profileRepository.findById(id);
    }

    /**
     * Get a user's profile by email
     * @param email The user's email
     * @return The user's profile
     */
    public Optional<Profile> getProfileByEmail(String email) {
        Optional<AppUser> userOpt = appUserRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            return profileRepository.findByUser(userOpt.get());
        }
        return Optional.empty();
    }

    /**
     * Create a new profile for a user
     * @param email The user's email
     * @return The created profile
     */
    @Transactional
    public Profile createProfile(String email) {
        Optional<AppUser> userOpt = appUserRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with email: " + email);
        }
        
        AppUser user = userOpt.get();
        
        // Check if profile already exists
        Optional<Profile> existingProfile = profileRepository.findByUser(user);
        if (existingProfile.isPresent()) {
            return existingProfile.get();
        }
        
        // Create new profile
        Profile profile = new Profile();
        profile.setUser(user);
        profile.setCreatedAt(Instant.now());
        
        return profileRepository.save(profile);
    }

    /**
     * Update an existing profile
     * @param id The profile ID
     * @param email The new email
     * @return The updated profile
     */
    @Transactional
    public Profile updateProfile(Integer id, String email) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found with ID: " + id));
        
        Optional<AppUser> userOpt = appUserRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with email: " + email);
        }
        
        AppUser user = userOpt.get();
        profile.setUser(user);
        
        return profileRepository.save(profile);
    }

    /**
     * Delete a profile
     * @param id The profile ID
     */
    @Transactional
    public void deleteProfile(Integer id) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found with ID: " + id));
        
        profileRepository.delete(profile);
    }

    /**
     * Get user preferences for recommendation
     * This method delegates to the RecommendationService
     * @param email The user's email
     * @return Map of user preferences
     */
    public Map<String, Object> getUserPreferencesForRecommendation(String email) {
        return recommendationService.getUserPreferencesForRecommendation(email);
    }

    /**
     * Get AppUser by email
     * @param email The user's email
     * @return The AppUser if found
     */
    public Optional<AppUser> getAppUserByEmail(String email) {
        return appUserRepository.findByEmail(email);
    }

    /**
     * Save a profile
     * @param profile The profile to save
     */
    public void saveProfile(Profile profile) {
        profileRepository.save(profile);
    }
    
    /**
     * Set all user preferences
     * This method delegates to the RecommendationService
     * @param email The user's email
     * @param preferences Map containing all user preferences
     */
    @Transactional
    public void setUserPreferences(String email, Map<String, Object> preferences) {
        recommendationService.setUserPreferences(email, preferences);
    }
}
