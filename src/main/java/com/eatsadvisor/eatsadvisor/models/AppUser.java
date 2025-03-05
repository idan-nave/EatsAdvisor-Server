package com.eatsadvisor.eatsadvisor.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "app_user", indexes = {
        @Index(name = "idx_user_username", columnList = "username"),
        @Index(name = "idx_user_email", columnList = "email")
}, uniqueConstraints = {
        @UniqueConstraint(name = "app_user_username_key", columnNames = {"username"}),
        @UniqueConstraint(name = "app_user_email_key", columnNames = {"email"}),
        @UniqueConstraint(name = "app_user_oauth_provider_id_key", columnNames = {"oauth_provider_id"})
})
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_user_id_gen")
    @SequenceGenerator(name = "app_user_id_gen", sequenceName = "app_user_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 255)
    @Column(name = "first_name")
    private String firstName;

    @Size(max = 255)
    @Column(name = "last_name")
    private String lastName;

    @Size(max = 255)
    @Column(name = "username")
    private String username;

    @Size(max = 255)
    @NotNull
    @Column(name = "email", nullable = false)
    private String email;

    @Size(max = 50)
    @NotNull
    @Column(name = "oauth_provider", nullable = false, length = 50)
    private String oauthProvider;

    @Size(max = 255)
    @NotNull
    @Column(name = "oauth_provider_id", nullable = false)
    private String oauthProviderId;

    @Column(name = "created_at")
    private Instant createdAt;

    @OneToMany(mappedBy = "user")
    private Set<AppUserRoleMapping> appUserRoleMappings = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<AppUserStatus> appUserStatuses = new LinkedHashSet<>();

    @OneToOne(mappedBy = "user")
    private Profile profile;

    @OneToMany(mappedBy = "user")
    private Set<RefreshToken> refreshTokens = new LinkedHashSet<>();

}