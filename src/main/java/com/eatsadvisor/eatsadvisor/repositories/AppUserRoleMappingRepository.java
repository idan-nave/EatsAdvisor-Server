package com.eatsadvisor.eatsadvisor.repositories;

import com.eatsadvisor.eatsadvisor.models.AppUserRoleMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AppUserRoleMappingRepository extends JpaRepository<AppUserRoleMapping, Integer>, JpaSpecificationExecutor<AppUserRoleMapping> {
}