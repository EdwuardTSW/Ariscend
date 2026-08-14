package com.ariscend.backend.repository;

import com.ariscend.backend.entity.FinancialGoal;
import com.ariscend.backend.entity.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FinancialGoalRepository extends JpaRepository<FinancialGoal, Long> {
    List<FinancialGoal> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, GoalStatus status);
    Optional<FinancialGoal> findByIdAndUserId(Long goalId, Long userId);
}
