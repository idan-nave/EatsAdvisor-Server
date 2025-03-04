package com.eatsadvisor.eatsadvisor.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "profile_flavor_preference", indexes = {
        @Index(name = "idx_profile_flavor_preference_profile_id", columnList = "profile_id"),
        @Index(name = "idx_profile_flavor_preference_flavor_id", columnList = "flavor_id")
})
public class ProfileFlavorPreference {
    @EmbeddedId
    private ProfileFlavorPreferenceId id;

    @MapsId("profileId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @MapsId("flavorId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "flavor_id", nullable = false)
    private Flavor flavor;

    @Column(name = "preference_level")
    private Integer preferenceLevel;

    @Column(name = "created_at")
    private Instant createdAt;

}