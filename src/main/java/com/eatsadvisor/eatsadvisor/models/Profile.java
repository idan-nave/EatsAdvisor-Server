package com.eatsadvisor.eatsadvisor.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "profile", schema = "public", indexes = {
        @Index(name = "idx_profile_user_id", columnList = "user_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "profile_user_id_key", columnNames = {"user_id"})
})
public class Profile {
    @Id
    @ColumnDefault("nextval('profile_id_seq')")
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

}