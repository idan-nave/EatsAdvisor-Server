package com.eatsadvisor.eatsadvisor.repositories;

import com.eatsadvisor.eatsadvisor.models.SpecialPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SpecialPreferenceRepository extends JpaRepository<SpecialPreference, Integer>, JpaSpecificationExecutor<SpecialPreference> {
}