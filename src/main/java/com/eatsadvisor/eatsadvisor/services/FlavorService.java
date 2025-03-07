package com.eatsadvisor.eatsadvisor.services;

import com.eatsadvisor.eatsadvisor.models.Flavor;
import com.eatsadvisor.eatsadvisor.repositories.FlavorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class FlavorService {
    private final FlavorRepository flavorRepository;

    public FlavorService(FlavorRepository flavorRepository) {
        this.flavorRepository = flavorRepository;
    }

    /**
     * Get all flavors
     * @return List of all flavors
     */
    public List<Flavor> getAllFlavors() {
        return flavorRepository.findAll();
    }

    /**
     * Get a flavor by ID
     * @param id The flavor ID
     * @return The flavor if found
     */
    public Optional<Flavor> getFlavorById(Integer id) {
        return flavorRepository.findById(id);
    }

    /**
     * Get a flavor by name
     * @param name The flavor name
     * @return The flavor if found
     */
    public Optional<Flavor> getFlavorByName(String name) {
        return flavorRepository.findByName(name);
    }

    /**
     * Search flavors by name (case insensitive)
     * @param name The name to search for
     * @return List of matching flavors
     */
    public List<Flavor> searchFlavorsByName(String name) {
        return flavorRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Create a new flavor
     * @param name The flavor name
     * @param description The flavor description (optional)
     * @return The created flavor
     */
    @Transactional
    public Flavor createFlavor(String name, String description) {
        // Check if flavor already exists
        if (flavorRepository.existsByName(name)) {
            throw new IllegalArgumentException("Flavor with name '" + name + "' already exists");
        }
        
        Flavor flavor = new Flavor();
        flavor.setName(name);
        flavor.setDescription(description);
        flavor.setCreatedAt(Instant.now());
        
        return flavorRepository.save(flavor);
    }

    /**
     * Update an existing flavor
     * @param id The flavor ID
     * @param name The new name (optional)
     * @param description The new description (optional)
     * @return The updated flavor
     */
    @Transactional
    public Flavor updateFlavor(Integer id, String name, String description) {
        Flavor flavor = flavorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Flavor not found with ID: " + id));
        
        // Update name if provided
        if (name != null && !name.isEmpty()) {
            // Check if the new name already exists for a different flavor
            Optional<Flavor> existingFlavor = flavorRepository.findByName(name);
            if (existingFlavor.isPresent() && !existingFlavor.get().getId().equals(id)) {
                throw new IllegalArgumentException("Flavor with name '" + name + "' already exists");
            }
            flavor.setName(name);
        }
        
        // Update description if provided
        if (description != null) {
            flavor.setDescription(description);
        }
        
        return flavorRepository.save(flavor);
    }

    /**
     * Delete a flavor
     * @param id The flavor ID
     */
    @Transactional
    public void deleteFlavor(Integer id) {
        // Check if flavor exists
        if (!flavorRepository.existsById(id)) {
            throw new RuntimeException("Flavor not found with ID: " + id);
        }
        
        flavorRepository.deleteById(id);
    }

    /**
     * Get flavors for a specific profile
     * @param profileId The profile ID
     * @return List of flavors for the profile
     */
    public List<Flavor> getFlavorsByProfileId(Integer profileId) {
        return flavorRepository.findByProfileId(profileId);
    }

    /**
     * Get flavors for a specific profile with a minimum preference level
     * @param profileId The profile ID
     * @param minPreferenceLevel The minimum preference level
     * @return List of flavors for the profile with the minimum preference level
     */
    public List<Flavor> getFlavorsByProfileIdAndMinPreferenceLevel(Integer profileId, Integer minPreferenceLevel) {
        return flavorRepository.findByProfileIdAndMinPreferenceLevel(profileId, minPreferenceLevel);
    }

    /**
     * Get the most popular flavors
     * @param limit The maximum number of flavors to return
     * @return List of flavors with their average preference levels
     */
    public List<Object[]> getMostPopularFlavors(Integer limit) {
        // This method would typically use a custom query in the repository
        // For now, we'll return a limited number of flavors
        return flavorRepository.findAll().stream()
                .limit(limit)
                .map(flavor -> new Object[]{flavor, 0.0})
                .toList();
    }
}
