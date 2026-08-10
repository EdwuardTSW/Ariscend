package com.ariscend.backend.repository;

import com.ariscend.backend.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Task> findByIdAndUserId(Long taskId, Long userId);
}
