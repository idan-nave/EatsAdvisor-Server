package com.eatsadvisor.eatsadvisor.services;

import com.eatsadvisor.eatsadvisor.models.AppUser;
import com.eatsadvisor.eatsadvisor.repositories.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AppUserService {
    private final AppUserRepository userRepository;

    public AppUserService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<AppUser> findAppUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public AppUser updateUserPreferences(AppUser updatedUser) {
        Optional<AppUser> existingUserOpt = userRepository.findById(Long.valueOf(updatedUser.getId()));

        if (existingUserOpt.isPresent()) {
            AppUser existingUser = existingUserOpt.get();
            // Commenting out preferences update
            // existingUser.setPreferences(updatedUser.getPreferences());
            return userRepository.save(existingUser);
        } else {
            throw new RuntimeException("User not found");
        }
    }
}
