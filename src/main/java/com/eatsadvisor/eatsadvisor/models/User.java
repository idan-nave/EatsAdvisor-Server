package com.eatsadvisor.eatsadvisor.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "users")  // Defines table name explicitly
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder  // Enables Lombok builder pattern
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name cannot be empty")  // Ensures name is not blank
    @Column(nullable = false)  // Makes it required in the DB
    private String name;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")  // Validates email format
    @Column(nullable = false, unique = true)  // Ensures uniqueness
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
