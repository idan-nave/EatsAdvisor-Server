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
@Table(name = "profile_allergy", indexes = {
        @Index(name = "idx_profile_allergy_profile_id", columnList = "profile_id"),
        @Index(name = "idx_profile_allergy_allergy_id", columnList = "allergy_id")
})
public class ProfileAllergy {
    @EmbeddedId
    private ProfileAllergyId id;

    @MapsId("profileId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @MapsId("allergyId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "allergy_id", nullable = false)
    private Allergy allergy;

    @Column(name = "created_at")
    private Instant createdAt;

}