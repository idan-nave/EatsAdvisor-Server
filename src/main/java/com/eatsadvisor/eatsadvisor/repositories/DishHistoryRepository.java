package com.eatsadvisor.eatsadvisor.repositories;

import com.eatsadvisor.eatsadvisor.models.DishHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DishHistoryRepository extends JpaRepository<DishHistory, Integer>, JpaSpecificationExecutor<DishHistory> {
}