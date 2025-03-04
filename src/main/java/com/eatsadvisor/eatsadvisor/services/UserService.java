package com.eatsadvisor.eatsadvisor.services;

import com.eatsadvisor.eatsadvisor.models.User;
import com.eatsadvisor.eatsadvisor.repositories.UserRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
