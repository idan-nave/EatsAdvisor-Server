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
@Table(name = "dish_history", indexes = {
        @Index(name = "idx_dish_history_profile_id", columnList = "profile_id"),
        @Index(name = "idx_dish_history_dish_id", columnList = "dish_id")
})
public class DishHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ColumnDefault("nextval('dish_history_id_seq')")
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "profile_id", nullable = false)
    private com.eatsadvisor.eatsadvisor.models.Profile profile;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "dish_id", nullable = false)
    private Dish dish;

    @Column(name = "user_rating")
    private Integer userRating;

    @Column(name = "created_at")
    private Instant createdAt;

}
