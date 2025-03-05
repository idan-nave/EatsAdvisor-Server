package com.eatsadvisor.eatsadvisor.controllers;

import com.eatsadvisor.eatsadvisor.models.AppUser;
import com.eatsadvisor.eatsadvisor.services.AppUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class AppUserController {

    private final AppUserService userService;

    public AppUserController(AppUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{email}")
    public ResponseEntity<Optional<AppUser>> getUserByEmail(@PathVariable String email) {
        Optional<AppUser> user = userService.findAppUserByEmail(email);
        return user.isPresent() ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

    @PostMapping("/update-preferences")
    public ResponseEntity<AppUser> updateUserPreferences(@RequestBody AppUser updatedUser) {
        AppUser savedUser = userService.updateUserPreferences(updatedUser);
        return ResponseEntity.ok(savedUser);
    }
}
