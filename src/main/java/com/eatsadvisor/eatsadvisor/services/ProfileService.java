package com.eatsadvisor.eatsadvisor.services;

import com.eatsadvisor.eatsadvisor.models.*;
import com.eatsadvisor.eatsadvisor.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final AppUserRepository appUserRepository;
    private final ProfileAllergyRepository profileAllergyRepository;
    private final AllergyRepository allergyRepository;
    private final ProfileFlavorPreferenceRepository profileFlavorPreferenceRepository;
    private final FlavorRepository flavorRepository;
    private final ProfileConstraintRepository profileConstraintRepository;
    private final ConstraintTypeRepository constraintTypeRepository;
    private final SpecialPreferenceRepository specialPreferenceRepository;
    private final DishHistoryRepository dishHistoryRepository;
    private final DishRepository dishRepository;

    public ProfileService(
            ProfileRepository profileRepository,
            AppUserRepository appUserRepository,
            ProfileAllergyRepository profileAllergyRepository,
            AllergyRepository allergyRepository,
            ProfileFlavorPreferenceRepository profileFlavorPreferenceRepository,
            FlavorRepository flavorRepository,
            ProfileConstraintRepository profileConstraintRepository,
            ConstraintTypeRepository constraintTypeRepository,
            SpecialPreferenceRepository specialPreferenceRepository,
            DishHistoryRepository dishHistoryRepository,
            DishRepository dishRepository) {
        this.profileRepository = profileRepository;
        this.appUserRepository = appUserRepository;
        this.profileAllergyRepository = profileAllergyRepository;
        this.allergyRepository = allergyRepository;
        this.profileFlavorPreferenceRepository = profileFlavorPreferenceRepository;
        this.flavorRepository = flavorRepository;
        this.profileConstraintRepository = profileConstraintRepository;
        this.constraintTypeRepository = constraintTypeRepository;
        this.specialPreferenceRepository = specialPreferenceRepository;
        this.dishHistoryRepository = dishHistoryRepository;
        this.dishRepository = dishRepository;
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
        profile.setCreatedAt(java.time.Instant.now());
        
        return profileRepository.save(profile);
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
        profileAllergy.setCreatedAt(java.time.Instant.now());
        
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
            preference.setCreatedAt(java.time.Instant.now());
        }
        
        return profileFlavorPreferenceRepository.save(preference);
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
        profileConstraint.setCreatedAt(java.time.Instant.now());
        
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
        specialPreference.setCreatedAt(java.time.Instant.now());
        
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
     * Get dish history for a profile
     * @param profileId The profile ID
     * @return List of dish history entries
     */
    public List<DishHistory> getDishHistoryByProfileId(Integer profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found with ID: " + profileId));
        
        return dishHistoryRepository.findByProfile(profile);
    }

    /**
     * Rate a dish
     * @param profileId The profile ID
     * @param dishId The dish ID
     * @param rating The rating (1-5)
     * @return The created or updated dish history entry
     */
    @Transactional
    public DishHistory rateDish(Integer profileId, Integer dishId, Integer rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found with ID: " + profileId));
        
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new RuntimeException("Dish not found with ID: " + dishId));
        
        // Check if already exists
        Optional<DishHistory> existingOpt = dishHistoryRepository.findByProfileAndDish(profile, dish);
        
        DishHistory dishHistory;
        if (existingOpt.isPresent()) {
            dishHistory = existingOpt.get();
            dishHistory.setUserRating(rating);
        } else {
            dishHistory = new DishHistory();
            dishHistory.setProfile(profile);
            dishHistory.setDish(dish);
            dishHistory.setUserRating(rating);
            dishHistory.setCreatedAt(java.time.Instant.now());
        }
        
        return dishHistoryRepository.save(dishHistory);
    }

    /**
     * Get user preferences for recommendation
     * This method collects all user preferences and formats them for the OpenAI recommendation API
     * @param email The user's email
     * @return Map of user preferences
     */
    public Map<String, Object> getUserPreferencesForRecommendation(String email) {
        Optional<Profile> profileOpt = getProfileByEmail(email);
        if (profileOpt.isEmpty()) {
            return new HashMap<>();
        }
        
        Profile profile = profileOpt.get();
        Map<String, Object> preferences = new HashMap<>();
        
        // Get allergies
        List<Allergy> allergies = getAllergiesByProfileId(profile.getId());
        if (!allergies.isEmpty()) {
            preferences.put("allergies", allergies.stream()
                    .map(Allergy::getName)
                    .collect(Collectors.toList()));
        }
        
        // Get flavor preferences
        List<ProfileFlavorPreference> flavorPreferences = getFlavorPreferencesByProfileId(profile.getId());
        if (!flavorPreferences.isEmpty()) {
            Map<String, Integer> flavorMap = new HashMap<>();
            for (ProfileFlavorPreference pfp : flavorPreferences) {
                flavorMap.put(pfp.getFlavor().getName(), pfp.getPreferenceLevel());
            }
            preferences.put("flavorPreferences", flavorMap);
        }
        
        // Get dietary constraints
        List<ConstraintType> constraints = getConstraintsByProfileId(profile.getId());
        if (!constraints.isEmpty()) {
            preferences.put("dietaryConstraints", constraints.stream()
                    .map(ConstraintType::getName)
                    .collect(Collectors.toList()));
        }
        
        // Get special preferences
        List<SpecialPreference> specialPreferences = getSpecialPreferencesByProfileId(profile.getId());
        if (!specialPreferences.isEmpty()) {
            preferences.put("specialPreferences", specialPreferences.stream()
                    .map(SpecialPreference::getDescription)
                    .collect(Collectors.toList()));
        }
        
        // Get dish history
        List<DishHistory> dishHistory = getDishHistoryByProfileId(profile.getId());
        if (!dishHistory.isEmpty()) {
            Map<String, Integer> dishMap = new HashMap<>();
            for (DishHistory dh : dishHistory) {
                dishMap.put(dh.getDish().getName(), dh.getUserRating());
            }
            preferences.put("dishHistory", dishMap);
        }
        
        return preferences;
    }
}
