package com.eatsadvisor.eatsadvisor.services;

import com.eatsadvisor.eatsadvisor.models.Allergy;
import com.eatsadvisor.eatsadvisor.models.Profile;
import com.eatsadvisor.eatsadvisor.models.ProfileAllergy;
import com.eatsadvisor.eatsadvisor.models.ProfileAllergyId;
import com.eatsadvisor.eatsadvisor.repositories.AllergyRepository;
import com.eatsadvisor.eatsadvisor.repositories.ProfileAllergyRepository;
import com.eatsadvisor.eatsadvisor.repositories.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ProfileAllergyService {
    private final ProfileAllergyRepository profileAllergyRepository;
    private final ProfileRepository profileRepository;
    private final AllergyRepository allergyRepository;

    public ProfileAllergyService(
            ProfileAllergyRepository profileAllergyRepository,
            ProfileRepository profileRepository,
            AllergyRepository allergyRepository) {
        this.profileAllergyRepository = profileAllergyRepository;
        this.profileRepository = profileRepository;
        this.allergyRepository = allergyRepository;
    }

    /**
     * Get all allergies for a profile
     * @param profileId The profile ID
     * @return List of allergies
     */
    public List<Allergy> getAllergiesByProfileId(Integer profileId) {
        return allergyRepository.findByProfileId(profileId);
    }

    /**
     * Add an allergy to a profile
     * @param profileId The profile ID
     * @param allergyId The allergy ID
     * @return The created profile allergy
     */
    @Transactional
    public ProfileAllergy addAllergyToProfile(Integer profileId, Integer allergyId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found with ID: " + profileId));
        
        Allergy allergy = allergyRepository.findById(allergyId)
                .orElseThrow(() -> new RuntimeException("Allergy not found with ID: " + allergyId));
        
        // Check if already exists
        Optional<ProfileAllergy> existingOpt = profileAllergyRepository.findByProfileAndAllergy(profile, allergy);
        if (existingOpt.isPresent()) {
            return existingOpt.get();
        }
        
        // Create new profile allergy
        ProfileAllergy profileAllergy = new ProfileAllergy();
        profileAllergy.setProfile(profile);
        profileAllergy.setAllergy(allergy);
        profileAllergy.setCreatedAt(Instant.now());
        
        return profileAllergyRepository.save(profileAllergy);
    }

    /**
     * Remove an allergy from a profile
     * @param profileId The profile ID
     * @param allergyId The allergy ID
     */
    @Transactional
    public void removeAllergyFromProfile(Integer profileId, Integer allergyId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found with ID: " + profileId));
        
        Allergy allergy = allergyRepository.findById(allergyId)
                .orElseThrow(() -> new RuntimeException("Allergy not found with ID: " + allergyId));
        
        profileAllergyRepository.deleteByProfileAndAllergy(profile, allergy);
    }

    /**
     * Process allergies for a profile
     * @param profile The profile
     * @param allergies List of allergy names
     */
    @Transactional
    public void processAllergies(Profile profile, List<String> allergies) {
        // Clear existing allergies
        List<ProfileAllergy> existingAllergies = profileAllergyRepository.findByProfile(profile);
        profileAllergyRepository.deleteAll(existingAllergies);
        
        // Add new allergies
        for (String allergyName : allergies) {
            if (allergyName == null || allergyName.trim().isEmpty()) {
                continue; // Skip empty allergy names
            }
            
            // Find or create allergy
            Allergy allergy = allergyRepository.findByName(allergyName)
                    .orElseGet(() -> {
                        Allergy newAllergy = new Allergy();
                        newAllergy.setName(allergyName);
                        newAllergy.setCreatedAt(Instant.now());
                        return allergyRepository.save(newAllergy);
                    });
            
            // Create profile allergy mapping with properly initialized ID
            ProfileAllergy profileAllergy = new ProfileAllergy();
            
            // Initialize the composite ID
            ProfileAllergyId id = new ProfileAllergyId();
            id.setProfileId(profile.getId());
            id.setAllergyId(allergy.getId());
            profileAllergy.setId(id);
            
            // Set the relationships
            profileAllergy.setProfile(profile);
            profileAllergy.setAllergy(allergy);
            profileAllergy.setCreatedAt(Instant.now());
            
            // Save the entity
            profileAllergyRepository.save(profileAllergy);
        }
    }
}
