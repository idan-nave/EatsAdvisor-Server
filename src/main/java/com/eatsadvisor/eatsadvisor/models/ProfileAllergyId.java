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
public class ProfileAllergyId implements Serializable {
    private static final long serialVersionUID = 5571738984800754971L;
    @NotNull
    @Column(name = "profile_id", nullable = false)
    private Integer profileId;

    @NotNull
    @Column(name = "allergy_id", nullable = false)
    private Integer allergyId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ProfileAllergyId entity = (ProfileAllergyId) o;
        return Objects.equals(this.allergyId, entity.allergyId) &&
                Objects.equals(this.profileId, entity.profileId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allergyId, profileId);
    }

}