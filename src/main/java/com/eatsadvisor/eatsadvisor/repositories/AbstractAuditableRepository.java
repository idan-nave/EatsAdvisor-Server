package com.eatsadvisor.eatsadvisor.repositories;

import org.springframework.data.jpa.domain.AbstractAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface AbstractAuditableRepository<T extends AbstractAuditable> extends JpaRepository<T, PK>, JpaSpecificationExecutor<T> {
}