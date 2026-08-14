package com.ariscend.backend.repository;

import com.ariscend.backend.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByUserIdOrderByActiveDescCreatedAtDesc(Long userId);
    Optional<Card> findByIdAndUserId(Long cardId, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT card FROM Card card WHERE card.id = :cardId AND card.user.id = :userId")
    Optional<Card> findOwnedForUpdate(@Param("cardId") Long cardId, @Param("userId") Long userId);
}
