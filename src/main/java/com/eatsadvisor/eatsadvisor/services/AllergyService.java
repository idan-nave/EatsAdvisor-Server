package com.eatsadvisor.eatsadvisor.services;

import com.eatsadvisor.eatsadvisor.models.Allergy;
import com.eatsadvisor.eatsadvisor.repositories.AllergyRepository;
import com.eatsadvisor.eatsadvisor.models.AppUser;
import com.eatsadvisor.eatsadvisor.models.AppUserStatus;
import com.eatsadvisor.eatsadvisor.models.StatusType;
import com.eatsadvisor.eatsadvisor.repositories.AppUserStatusRepository;
import com.eatsadvisor.eatsadvisor.repositories.StatusTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class AllergyService {
    private final AllergyRepository allergyRepository;
    private final AppUserStatusRepository appUserStatusRepository;
    private final StatusTypeRepository statusTypeRepository;

    public AllergyService(AllergyRepository allergyRepository, AppUserStatusRepository appUserStatusRepository, StatusTypeRepository statusTypeRepository) {
        this.allergyRepository = allergyRepository;
        this.appUserStatusRepository = appUserStatusRepository;
        this.statusTypeRepository = statusTypeRepository;
    }

    /**
     * Get all allergies
     * @return List of all allergies
     */
    public List<Allergy> getAllAllergies() {
        return allergyRepository.findAll();
    }

    /**
     * Get an allergy by ID
     * @param id The allergy ID
     * @return The allergy if found
     */
    public Optional<Allergy> getAllergyById(Integer id) {
        return allergyRepository.findById(id);
    }

    /**
     * Get an allergy by name
     * @param name The allergy name
     * @return The allergy if found
     */
    public Optional<Allergy> getAllergyByName(String name) {
        return allergyRepository.findByName(name);
    }

    /**
     * Search allergies by name (case insensitive)
     * @param name The name to search for
     * @return List of matching allergies
     */
    public List<Allergy> searchAllergiesByName(String name) {
        return allergyRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Create a new allergy
     * @param name The allergy name
     * @param description The allergy description (optional)
     * @return The created allergy
     */
    @Transactional
    public Allergy createAllergy(String name, String description) {
        // Check if allergy already exists
        if (allergyRepository.existsByName(name)) {
            throw new IllegalArgumentException("Allergy with name '" + name + "' already exists");
        }

        Allergy allergy = new Allergy();
        allergy.setName(name);
        allergy.setDescription(description);
        allergy.setCreatedAt(Instant.now());

        return allergyRepository.save(allergy);
    }

    /**
     * Update an existing allergy
     * @param id The allergy ID
     * @param name The new name (optional)
     * @param description The new description (optional)
     * @return The updated allergy
     */
    @Transactional
    public Allergy updateAllergy(Integer id, String name, String description) {
        Allergy allergy = allergyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Allergy not found with ID: " + id));

        // Update name if provided
        if (name != null && !name.isEmpty()) {
            // Check if the new name already exists for a different allergy
            Optional<Allergy> existingAllergy = allergyRepository.findByName(name);
            if (existingAllergy.isPresent() && !existingAllergy.get().getId().equals(id)) {
                throw new IllegalArgumentException("Allergy with name '" + name + "' already exists");
            }
            allergy.setName(name);
        }

        // Update description if provided
        if (description != null) {
            allergy.setDescription(description);
        }

        return allergyRepository.save(allergy);
    }

    /**
     * Delete an allergy
     * @param id The allergy ID
     */
    @Transactional
    public void deleteAllergy(Integer id) {
        // Check if allergy exists
        if (!allergyRepository.existsById(id)) {
            throw new RuntimeException("Allergy not found with ID: " + id);
        }

        allergyRepository.deleteById(id);
    }

    /**
     * Get allergies for a specific profile
     * @param profileId The profile ID
     * @return List of allergies for the profile
     */
    @Transactional
    public void setAllergyStatus(AppUser user, String statusTypeName) {
        StatusType statusType = statusTypeRepository.findByName(statusTypeName)
                .orElseThrow(() -> new IllegalArgumentException("StatusType with name '" + statusTypeName + "' not found"));

        AppUserStatus appUserStatus = new AppUserStatus();
        appUserStatus.setUser(user);
        appUserStatus.setStatusType(statusType);
        appUserStatus.setCreatedAt(Instant.now());

        appUserStatusRepository.save(appUserStatus);
    }

    public List<Allergy> getAllergiesByProfileId(Integer profileId) {
        return allergyRepository.findByProfileId(profileId);
    }

    /**
     * Get the count of profiles with a specific allergy
     * @param allergyId The allergy ID
     * @return The number of profiles with the allergy
     */
    public long getProfileCountByAllergyId(Integer allergyId) {
        return allergyRepository.findById(allergyId)
                .map(allergy -> (long) allergyRepository.findByProfileId(allergyId).size())
                .orElse(0L);
    }
}
