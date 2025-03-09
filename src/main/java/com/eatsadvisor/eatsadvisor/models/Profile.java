package com.eatsadvisor.eatsadvisor.models;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Data
@Entity
@Table(name = "profile", indexes = {
        @Index(name = "idx_profile_user_id", columnList = "user_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "profile_user_id_key", columnNames = {"user_id"})
})
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private AppUser user;

    @Column(name = "created_at")
    private Instant createdAt;

}
