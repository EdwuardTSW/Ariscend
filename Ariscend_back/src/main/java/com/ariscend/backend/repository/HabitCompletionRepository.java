package com.ariscend.backend.repository;

import com.ariscend.backend.entity.HabitCompletion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface HabitCompletionRepository extends JpaRepository<HabitCompletion, Long> {

    boolean existsByHabitIdAndCompletedDate(Long habitId, LocalDate completedDate);

    List<HabitCompletion> findByHabitIdOrderByCompletedDateDesc(Long habitId);
}