package com.ariscend.backend.repository;

import com.ariscend.backend.entity.Habit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HabitRepository extends JpaRepository<Habit, Long> {

    List<Habit> findByUserIdAndActiveTrueOrderByCreatedAtDesc(Long userId);

    Optional<Habit> findByIdAndUserId(Long id, Long userId);
}