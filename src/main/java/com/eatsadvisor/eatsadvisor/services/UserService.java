package com.eatsadvisor.eatsadvisor.services;

import com.eatsadvisor.eatsadvisor.repositories.AppUserRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AppUserService {
    private final AppUserRepository appUserRepository;

    public AppUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public Optional<AppUser> findUserByEmail(String email) {
        return appUserRepository.findByEmail(email);
    }
}
