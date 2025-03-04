package com.eatsadvisor.eatsadvisor.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StatusTypeRepository extends JpaRepository<StatusType, Integer>, JpaSpecificationExecutor<StatusType> {
}