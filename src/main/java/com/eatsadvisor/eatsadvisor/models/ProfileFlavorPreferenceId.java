package com.eatsadvisor.eatsadvisor.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class ProfileFlavorPreferenceId implements Serializable {
    private static final long serialVersionUID = -8096164123754538549L;
    @NotNull
    @Column(name = "profile_id", nullable = false)
    private Integer profileId;

    @NotNull
    @Column(name = "flavor_id", nullable = false)
    private Integer flavorId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ProfileFlavorPreferenceId entity = (ProfileFlavorPreferenceId) o;
        return Objects.equals(this.profileId, entity.profileId) &&
                Objects.equals(this.flavorId, entity.flavorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profileId, flavorId);
    }

}