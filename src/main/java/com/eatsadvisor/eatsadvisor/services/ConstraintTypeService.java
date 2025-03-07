package com.eatsadvisor.eatsadvisor.services;

import com.eatsadvisor.eatsadvisor.models.ConstraintType;
import com.eatsadvisor.eatsadvisor.repositories.ConstraintTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ConstraintTypeService {
    private final ConstraintTypeRepository constraintTypeRepository;

    public ConstraintTypeService(ConstraintTypeRepository constraintTypeRepository) {
        this.constraintTypeRepository = constraintTypeRepository;
    }

    /**
     * Get all constraint types
     * @return List of all constraint types
     */
    public List<ConstraintType> getAllConstraintTypes() {
        return constraintTypeRepository.findAll();
    }

    /**
     * Get a constraint type by ID
     * @param id The constraint type ID
     * @return The constraint type if found
     */
    public Optional<ConstraintType> getConstraintTypeById(Integer id) {
        return constraintTypeRepository.findById(id);
    }

    /**
     * Get a constraint type by name
     * @param name The constraint type name
     * @return The constraint type if found
     */
    public Optional<ConstraintType> getConstraintTypeByName(String name) {
        return constraintTypeRepository.findByName(name);
    }

    /**
     * Search constraint types by name (case insensitive)
     * @param name The name to search for
     * @return List of matching constraint types
     */
    public List<ConstraintType> searchConstraintTypesByName(String name) {
        return constraintTypeRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Create a new constraint type
     * @param name The constraint type name
     * @return The created constraint type
     */
    @Transactional
    public ConstraintType createConstraintType(String name) {
        // Check if constraint type already exists
        if (constraintTypeRepository.existsByName(name)) {
            throw new IllegalArgumentException("Constraint type with name '" + name + "' already exists");
        }
        
        ConstraintType constraintType = new ConstraintType();
        constraintType.setName(name);
        constraintType.setCreatedAt(Instant.now());
        
        return constraintTypeRepository.save(constraintType);
    }

    /**
     * Update an existing constraint type
     * @param id The constraint type ID
     * @param name The new name
     * @return The updated constraint type
     */
    @Transactional
    public ConstraintType updateConstraintType(Integer id, String name) {
        ConstraintType constraintType = constraintTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Constraint type not found with ID: " + id));
        
        // Update name if provided
        if (name != null && !name.isEmpty()) {
            // Check if the new name already exists for a different constraint type
            Optional<ConstraintType> existingConstraintType = constraintTypeRepository.findByName(name);
            if (existingConstraintType.isPresent() && !existingConstraintType.get().getId().equals(id)) {
                throw new IllegalArgumentException("Constraint type with name '" + name + "' already exists");
            }
            constraintType.setName(name);
        }
        
        return constraintTypeRepository.save(constraintType);
    }

    /**
     * Delete a constraint type
     * @param id The constraint type ID
     */
    @Transactional
    public void deleteConstraintType(Integer id) {
        // Check if constraint type exists
        if (!constraintTypeRepository.existsById(id)) {
            throw new RuntimeException("Constraint type not found with ID: " + id);
        }
        
        constraintTypeRepository.deleteById(id);
    }

    /**
     * Get constraint types for a specific profile
     * @param profileId The profile ID
     * @return List of constraint types for the profile
     */
    public List<ConstraintType> getConstraintTypesByProfileId(Integer profileId) {
        return constraintTypeRepository.findByProfileId(profileId);
    }

    /**
     * Get the count of profiles with a specific constraint type
     * @param constraintTypeId The constraint type ID
     * @return The number of profiles with the constraint type
     */
    public long getProfileCountByConstraintTypeId(Integer constraintTypeId) {
        return constraintTypeRepository.countByConstraintTypeId(constraintTypeId);
    }

    /**
     * Get or create a constraint type by name
     * @param name The constraint type name
     * @return The existing or created constraint type
     */
    @Transactional
    public ConstraintType getOrCreateConstraintType(String name) {
        Optional<ConstraintType> existingConstraintType = constraintTypeRepository.findByName(name);
        if (existingConstraintType.isPresent()) {
            return existingConstraintType.get();
        }
        
        return createConstraintType(name);
    }

    /**
     * Get common dietary constraint types
     * @return List of common dietary constraint types
     */
    public List<ConstraintType> getCommonDietaryConstraintTypes() {
        // Common dietary constraints
        String[] commonConstraints = {
            "Vegetarian",
            "Vegan",
            "Gluten-Free",
            "Dairy-Free",
            "Nut-Free",
            "Kosher",
            "Halal",
            "Low-Carb",
            "Keto",
            "Paleo"
        };
        
        // Create any that don't exist
        for (String constraint : commonConstraints) {
            getOrCreateConstraintType(constraint);
        }
        
        // Return all constraint types
        return getAllConstraintTypes();
    }
}
