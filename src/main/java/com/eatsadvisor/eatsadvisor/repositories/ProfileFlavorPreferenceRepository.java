package com.eatsadvisor.eatsadvisor.repositories;

import com.eatsadvisor.eatsadvisor.models.ProfileFlavorPreference;
import com.eatsadvisor.eatsadvisor.models.ProfileFlavorPreferenceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProfileFlavorPreferenceRepository extends JpaRepository<ProfileFlavorPreference, ProfileFlavorPreferenceId>, JpaSpecificationExecutor<ProfileFlavorPreference> {
}