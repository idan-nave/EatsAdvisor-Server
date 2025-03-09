package com.eatsadvisor.eatsadvisor.services;

import com.eatsadvisor.eatsadvisor.models.Flavor;
import com.eatsadvisor.eatsadvisor.models.Profile;
import com.eatsadvisor.eatsadvisor.models.ProfileFlavorPreference;
import com.eatsadvisor.eatsadvisor.models.ProfileFlavorPreferenceId;
import com.eatsadvisor.eatsadvisor.repositories.FlavorRepository;
import com.eatsadvisor.eatsadvisor.repositories.ProfileFlavorPreferenceRepository;
import com.eatsadvisor.eatsadvisor.repositories.ProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProfileFlavorPreferenceService {
    private static final Logger logger = LoggerFactory.getLogger(ProfileFlavorPreferenceService.class);
    private final ProfileFlavorPreferenceRepository profileFlavorPreferenceRepository;
    private final ProfileRepository profileRepository;
    private final FlavorRepository flavorRepository;

    public ProfileFlavorPreferenceService(
            ProfileFlavorPreferenceRepository profileFlavorPreferenceRepository,
            ProfileRepository profileRepository,
            FlavorRepository flavorRepository) {
        this.profileFlavorPreferenceRepository = profileFlavorPreferenceRepository;
        this.profileRepository = profileRepository;
        this.flavorRepository = flavorRepository;
    }

    /**
     * Get all flavor preferences for a profile
     * @param profileId The profile ID
     * @return List of flavor preferences
     */
    public List<ProfileFlavorPreference> getFlavorPreferencesByProfileId(Integer profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found with ID: " + profileId));
        
        return profileFlavorPreferenceRepository.findByProfile(profile);
    }

    /**
     * Set a flavor preference for a profile
     * @param profileId The profile ID
     * @param flavorId The flavor ID
     * @param preferenceLevel The preference level (1-10)
     * @return The updated flavor preference
     */
    @Transactional
    public ProfileFlavorPreference setFlavorPreference(Integer profileId, Integer flavorId, Integer preferenceLevel) {
        if (preferenceLevel < 1 || preferenceLevel > 10) {
            throw new IllegalArgumentException("Preference level must be between 1 and 10");
        }
        
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found with ID: " + profileId));
        
        Flavor flavor = flavorRepository.findById(flavorId)
                .orElseThrow(() -> new RuntimeException("Flavor not found with ID: " + flavorId));
        
        // Check if already exists
        Optional<ProfileFlavorPreference> existingOpt = profileFlavorPreferenceRepository.findByProfileAndFlavor(profile, flavor);
        
        ProfileFlavorPreference preference;
        if (existingOpt.isPresent()) {
            preference = existingOpt.get();
            preference.setPreferenceLevel(preferenceLevel);
        } else {
            preference = new ProfileFlavorPreference();
            preference.setProfile(profile);
            preference.setFlavor(flavor);
            preference.setPreferenceLevel(preferenceLevel);
            preference.setCreatedAt(Instant.now());
        }
        
        return profileFlavorPreferenceRepository.save(preference);
    }

    /**
     * Process flavor preferences for a profile
     * @param profile The profile
     * @param flavorPrefs Map of flavor names to preference levels
     */
    @Transactional
    public void processFlavorPreferences(Profile profile, Map<String, Integer> flavorPrefs) {
        for (Map.Entry<String, Integer> entry : flavorPrefs.entrySet()) {
            String flavorName = entry.getKey();
            Integer rating = entry.getValue();

            // Validate rating
            if (rating < 1 || rating > 10) {
                logger.warn("Invalid flavor rating for {}: {}. Must be between 1 and 10.", flavorName, rating);
                continue;
            }

            // Find or create flavor
            Flavor flavor = flavorRepository.findByName(flavorName)
                    .orElseGet(() -> {
                        Flavor newFlavor = new Flavor();
                        newFlavor.setName(flavorName);
                        newFlavor.setCreatedAt(Instant.now());
                        return flavorRepository.save(newFlavor);
                    });

            // Find existing preference
            Optional<ProfileFlavorPreference> existingPreference = profileFlavorPreferenceRepository.findByProfileAndFlavor(profile, flavor);

            if (existingPreference.isPresent()) {
                // Update existing preference with new rating
                ProfileFlavorPreference preference = existingPreference.get();
                preference.setPreferenceLevel(rating);
                profileFlavorPreferenceRepository.save(preference);
            } else {
                // Create new preference record with updated rating
                ProfileFlavorPreference preference = new ProfileFlavorPreference();
                preference.setProfile(profile);
                preference.setFlavor(flavor);
                preference.setPreferenceLevel(rating);
                preference.setCreatedAt(Instant.now());
                profileFlavorPreferenceRepository.save(preference);
            }
        }
        profileFlavorPreferenceRepository.flush();
    }
}
