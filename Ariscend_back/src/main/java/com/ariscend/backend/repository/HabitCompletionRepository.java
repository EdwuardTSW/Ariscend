package com.ariscend.backend.repository;

import com.ariscend.backend.entity.HabitCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface HabitCompletionRepository extends JpaRepository<HabitCompletion, Long> {

    boolean existsByHabitIdAndCompletedDate(Long habitId, LocalDate completedDate);

    List<HabitCompletion> findByHabitIdOrderByCompletedDateDesc(Long habitId);

    @Query("""
            SELECT completion
            FROM HabitCompletion completion
            WHERE completion.habit.user.id = :userId
              AND completion.habit.active = true
            ORDER BY completion.completedDate DESC
            """)
    List<HabitCompletion> findActiveHabitCompletionsByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT completion.habit.id
            FROM HabitCompletion completion
            WHERE completion.habit.user.id = :userId
              AND completion.completedDate = :completedDate
            """)
    List<Long> findCompletedHabitIds(
            @Param("userId") Long userId,
            @Param("completedDate") LocalDate completedDate
    );
}
