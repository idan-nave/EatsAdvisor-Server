package com.eatsadvisor.eatsadvisor.repositories;

import com.eatsadvisor.eatsadvisor.models.ProfileConstraint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProfileConstraintRepository extends JpaRepository<ProfileConstraint, Integer>, JpaSpecificationExecutor<ProfileConstraint> {
}