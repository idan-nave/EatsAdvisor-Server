package com.eatsadvisor.eatsadvisor.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequestMapping("/workers")
public class WorkerController {
    @GetMapping("/tasks")
    public String getWorkerTasks(@AuthenticationPrincipal Jwt jwt) {
        return "Worker email: " + jwt.getClaim("email") + " - Assigned orders retrieved!";
    }
}
