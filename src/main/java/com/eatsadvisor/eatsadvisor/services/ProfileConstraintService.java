package com.eatsadvisor.eatsadvisor.services;

import com.eatsadvisor.eatsadvisor.models.ConstraintType;
import com.eatsadvisor.eatsadvisor.models.Profile;
import com.eatsadvisor.eatsadvisor.models.ProfileConstraint;
import com.eatsadvisor.eatsadvisor.repositories.ConstraintTypeRepository;
import com.eatsadvisor.eatsadvisor.repositories.ProfileConstraintRepository;
import com.eatsadvisor.eatsadvisor.repositories.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ProfileConstraintService {
    private final ProfileConstraintRepository profileConstraintRepository;
    private final ProfileRepository profileRepository;
    private final ConstraintTypeRepository constraintTypeRepository;

    public ProfileConstraintService(
            ProfileConstraintRepository profileConstraintRepository,
            ProfileRepository profileRepository,
            ConstraintTypeRepository constraintTypeRepository) {
        this.profileConstraintRepository = profileConstraintRepository;
        this.profileRepository = profileRepository;
        this.constraintTypeRepository = constraintTypeRepository;
    }

    /**
     * Get all dietary constraints for a profile
     * @param profileId The profile ID
     * @return List of constraint types
     */
    public List<ConstraintType> getConstraintsByProfileId(Integer profileId) {
        return constraintTypeRepository.findByProfileId(profileId);
    }

    /**
     * Add a dietary constraint to a profile
     * @param profileId The profile ID
     * @param constraintTypeId The constraint type ID
     * @return The created profile constraint
     */
    @Transactional
    public ProfileConstraint addConstraintToProfile(Integer profileId, Integer constraintTypeId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found with ID: " + profileId));
        
        ConstraintType constraintType = constraintTypeRepository.findById(constraintTypeId)
                .orElseThrow(() -> new RuntimeException("Constraint type not found with ID: " + constraintTypeId));
        
        // Check if already exists
        Optional<ProfileConstraint> existingOpt = profileConstraintRepository.findByProfileAndConstraintType(profile, constraintType);
        if (existingOpt.isPresent()) {
            return existingOpt.get();
        }
        
        // Create new profile constraint
        ProfileConstraint profileConstraint = new ProfileConstraint();
        profileConstraint.setProfile(profile);
        profileConstraint.setConstraintType(constraintType);
        profileConstraint.setCreatedAt(Instant.now());
        
        return profileConstraintRepository.save(profileConstraint);
    }

    /**
     * Remove a dietary constraint from a profile
     * @param profileId The profile ID
     * @param constraintTypeId The constraint type ID
     */
    @Transactional
    public void removeConstraintFromProfile(Integer profileId, Integer constraintTypeId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found with ID: " + profileId));
        
        ConstraintType constraintType = constraintTypeRepository.findById(constraintTypeId)
                .orElseThrow(() -> new RuntimeException("Constraint type not found with ID: " + constraintTypeId));
        
        profileConstraintRepository.deleteByProfileAndConstraintType(profile, constraintType);
    }

    /**
     * Process dietary constraints for a profile
     * @param profile The profile
     * @param constraints List of constraint type names
     */
    @Transactional
    public void processConstraints(Profile profile, List<String> constraints) {
        // Clear existing constraints
        List<ProfileConstraint> existingConstraints = profileConstraintRepository.findByProfile(profile);
        profileConstraintRepository.deleteAll(existingConstraints);
        
        // Add new constraints
        for (String constraintName : constraints) {
            // Find or create constraint type
            ConstraintType constraintType = constraintTypeRepository.findByName(constraintName)
                    .orElseGet(() -> {
                        ConstraintType newConstraintType = new ConstraintType();
                        newConstraintType.setName(constraintName);
                        newConstraintType.setCreatedAt(Instant.now());
                        return constraintTypeRepository.save(newConstraintType);
                    });
            
            // Create profile constraint mapping
            ProfileConstraint profileConstraint = new ProfileConstraint();
            profileConstraint.setProfile(profile);
            profileConstraint.setConstraintType(constraintType);
            profileConstraint.setCreatedAt(Instant.now());
            profileConstraintRepository.save(profileConstraint);
        }
    }
}
