package com.ariscend.backend.repository;

import com.ariscend.backend.entity.FinanceSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FinanceSettingsRepository extends JpaRepository<FinanceSettings, Long> {
    Optional<FinanceSettings> findByUserId(Long userId);
}
