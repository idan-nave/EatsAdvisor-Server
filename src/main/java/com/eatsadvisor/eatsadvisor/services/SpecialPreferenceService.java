package com.eatsadvisor.eatsadvisor.services;

import com.eatsadvisor.eatsadvisor.models.Profile;
import com.eatsadvisor.eatsadvisor.models.SpecialPreference;
import com.eatsadvisor.eatsadvisor.repositories.ProfileRepository;
import com.eatsadvisor.eatsadvisor.repositories.SpecialPreferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class SpecialPreferenceService {
    private final SpecialPreferenceRepository specialPreferenceRepository;
    private final ProfileRepository profileRepository;

    public SpecialPreferenceService(
            SpecialPreferenceRepository specialPreferenceRepository,
            ProfileRepository profileRepository) {
        this.specialPreferenceRepository = specialPreferenceRepository;
        this.profileRepository = profileRepository;
    }

    /**
     * Get all special preferences for a profile
     * @param profileId The profile ID
     * @return List of special preferences
     */
    public List<SpecialPreference> getSpecialPreferencesByProfileId(Integer profileId) {
        return specialPreferenceRepository.findByProfileId(profileId);
    }

    /**
     * Add a special preference to a profile
     * @param profileId The profile ID
     * @param description The special preference description
     * @return The created special preference
     */
    @Transactional
    public SpecialPreference addSpecialPreference(Integer profileId, String description) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found with ID: " + profileId));
        
        SpecialPreference specialPreference = new SpecialPreference();
        specialPreference.setProfile(profile);
        specialPreference.setDescription(description);
        specialPreference.setCreatedAt(Instant.now());
        
        return specialPreferenceRepository.save(specialPreference);
    }

    /**
     * Delete a special preference
     * @param specialPreferenceId The special preference ID
     */
    @Transactional
    public void deleteSpecialPreference(Integer specialPreferenceId) {
        specialPreferenceRepository.deleteById(specialPreferenceId);
    }

    /**
     * Process special preferences for a profile
     * @param profile The profile
     * @param specialPrefs List of special preference descriptions
     */
    @Transactional
    public void processSpecialPreferences(Profile profile, List<String> specialPrefs) {
        // Clear existing special preferences
        List<SpecialPreference> existingPrefs = specialPreferenceRepository.findByProfile(profile);
        specialPreferenceRepository.deleteAll(existingPrefs);
        
        // Add new special preferences
        for (String description : specialPrefs) {
            if (description == null || description.trim().isEmpty()) {
                continue;
            }
            
            // Create special preference
            SpecialPreference specialPreference = new SpecialPreference();
            specialPreference.setProfile(profile);
            specialPreference.setDescription(description);
            specialPreference.setCreatedAt(Instant.now());
            specialPreferenceRepository.save(specialPreference);
        }
    }
}
