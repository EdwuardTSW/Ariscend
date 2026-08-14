package com.ariscend.backend.repository;

import com.ariscend.backend.entity.FinancialStatus;
import com.ariscend.backend.entity.GoalContribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface GoalContributionRepository extends JpaRepository<GoalContribution, Long> {
    List<GoalContribution> findByGoalIdOrderByContributionDateDescCreatedAtDesc(Long goalId);

    @Query("""
            SELECT contribution FROM GoalContribution contribution
            WHERE contribution.id = :contributionId
              AND contribution.goal.id = :goalId
              AND contribution.goal.user.id = :userId
            """)
    Optional<GoalContribution> findOwned(
            @Param("contributionId") Long contributionId,
            @Param("goalId") Long goalId,
            @Param("userId") Long userId
    );

    @Query("""
            SELECT COALESCE(SUM(contribution.amount), 0) FROM GoalContribution contribution
            WHERE contribution.goal.id = :goalId
              AND contribution.status = com.ariscend.backend.entity.FinancialStatus.ACTIVE
            """)
    BigDecimal sumActiveByGoalId(@Param("goalId") Long goalId);

    boolean existsByGoalIdAndStatus(Long goalId, FinancialStatus status);
}
