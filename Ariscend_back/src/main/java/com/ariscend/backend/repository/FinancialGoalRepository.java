package com.ariscend.backend.repository;

import com.ariscend.backend.entity.FinancialGoal;
import com.ariscend.backend.entity.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface FinancialGoalRepository extends JpaRepository<FinancialGoal, Long> {
    List<FinancialGoal> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, GoalStatus status);
    Optional<FinancialGoal> findByIdAndUserId(Long goalId, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT goal FROM FinancialGoal goal WHERE goal.id = :goalId AND goal.user.id = :userId")
    Optional<FinancialGoal> findOwnedForUpdate(@Param("goalId") Long goalId, @Param("userId") Long userId);

}
