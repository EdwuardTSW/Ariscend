package com.ariscend.backend.repository;

import com.ariscend.backend.entity.CategoryType;
import com.ariscend.backend.entity.TransactionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransactionCategoryRepository extends JpaRepository<TransactionCategory, Long> {

    @Query("""
            SELECT category FROM TransactionCategory category
            WHERE category.active = true
              AND (category.systemDefined = true OR category.user.id = :userId)
              AND (:type IS NULL OR category.type = :type)
            ORDER BY category.systemDefined DESC, category.name ASC
            """)
    List<TransactionCategory> findAvailable(
            @Param("userId") Long userId,
            @Param("type") CategoryType type
    );

    @Query("""
            SELECT category FROM TransactionCategory category
            WHERE category.id = :categoryId
              AND category.active = true
              AND (category.systemDefined = true OR category.user.id = :userId)
            """)
    Optional<TransactionCategory> findAvailableById(
            @Param("categoryId") Long categoryId,
            @Param("userId") Long userId
    );

    Optional<TransactionCategory> findByIdAndUserIdAndSystemDefinedFalse(Long id, Long userId);

    Optional<TransactionCategory> findBySystemKey(String systemKey);
    List<TransactionCategory> findAllBySystemDefinedTrueAndNameAndTypeOrderByIdAsc(
            String name,
            CategoryType type
    );

    @Query("""
            SELECT CASE WHEN COUNT(category) > 0 THEN true ELSE false END
            FROM TransactionCategory category
            WHERE category.active = true
              AND category.type = :type
              AND LOWER(category.name) = LOWER(:name)
              AND (category.systemDefined = true OR category.user.id = :userId)
              AND (:excludedId IS NULL OR category.id <> :excludedId)
            """)
    boolean existsAvailableName(@Param("userId") Long userId, @Param("type") CategoryType type,
            @Param("name") String name, @Param("excludedId") Long excludedId);
}
