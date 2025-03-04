package com.eatsadvisor.eatsadvisor.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AppUserStatusRepository extends JpaRepository<AppUserStatus, Integer>, JpaSpecificationExecutor<AppUserStatus> {
}