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
@Table(name = "app_user_status", schema = "public", indexes = {
        @Index(name = "idx_user_status_user_id", columnList = "user_id"),
        @Index(name = "idx_user_status_status_id", columnList = "status_type_id")
})
public class AppUserStatus {
    @Id
    @ColumnDefault("nextval('app_user_status_id_seq')")
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "status_type_id", nullable = false)
    private com.eatsadvisor.eatsadvisor.models.StatusType statusType;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

}