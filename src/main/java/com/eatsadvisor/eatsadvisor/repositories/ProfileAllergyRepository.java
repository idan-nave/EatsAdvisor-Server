package com.eatsadvisor.eatsadvisor.repositories;

import com.eatsadvisor.eatsadvisor.models.ProfileAllergy;
import com.eatsadvisor.eatsadvisor.models.ProfileAllergyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProfileAllergyRepository extends JpaRepository<ProfileAllergy, ProfileAllergyId>, JpaSpecificationExecutor<ProfileAllergy> {
}