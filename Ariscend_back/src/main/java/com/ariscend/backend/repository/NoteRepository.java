package com.ariscend.backend.repository;

import com.ariscend.backend.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {

    Optional<Note> findByIdAndUserId(Long noteId, Long userId);

    @Query("""
            SELECT note
            FROM Note note
            WHERE note.user.id = :userId
              AND (:pinned IS NULL OR note.pinned = :pinned)
              AND (:archived IS NULL OR note.archived = :archived)
              AND (
                    :query IS NULL
                    OR LOWER(COALESCE(note.title, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(note.content) LIKE LOWER(CONCAT('%', :query, '%'))
              )
            """)
    Page<Note> findByUserAndFilters(
            @Param("userId") Long userId,
            @Param("pinned") Boolean pinned,
            @Param("archived") Boolean archived,
            @Param("query") String query,
            Pageable pageable
    );
}
