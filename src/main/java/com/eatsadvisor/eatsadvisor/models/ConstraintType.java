package com.eatsadvisor.eatsadvisor.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "constraint_type", uniqueConstraints = {
        @UniqueConstraint(name = "constraint_type_name_key", columnNames = {"name"})
})
public class ConstraintType {
    @Id
    @ColumnDefault("nextval('constraint_type_id_seq')")
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "created_at")
    private Instant createdAt;

}