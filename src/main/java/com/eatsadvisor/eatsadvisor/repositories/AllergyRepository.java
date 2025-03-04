package com.eatsadvisor.eatsadvisor.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AllergyRepository extends JpaRepository<Allergy, Integer>, JpaSpecificationExecutor<Allergy> {
}