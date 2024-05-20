package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.GroupCategory;
import com.hcmus.mentor.backend.domain.constant.GroupCategoryStatus;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupCategoryRepository extends JpaRepository<GroupCategory, String> {
    @NotNull
    Optional<GroupCategory> findById(String id);

    GroupCategory findByName(String name);

    boolean existsByName(String name);

    List<GroupCategory> findByIdIn(List<String> ids);

    List<GroupCategory> findAllByStatus(GroupCategoryStatus status);

    @Query("SELECT gc " +
            "FROM GroupCategory gc " +
            "WHERE (:name IS NULL OR gc.name LIKE CONCAT('%', :name, '%')) " +
            "AND (:description IS NULL OR gc.description LIKE CONCAT('%', :description, '%')) " +
            "AND (:status IS NULL OR gc.status = :status) " +
            "ORDER BY gc.createdDate DESC LIMIT :pageSize OFFSET :offset")
    List<GroupCategory> findGroupCategoriesBySearchConditions(
            @Param("name") String name,
            @Param("description") String description,
            @Param("status") GroupCategoryStatus status,
            @Param("pageSize") int pageSize,
            @Param("offset") int offset);

    @Query("SELECT gc " +
            "FROM GroupCategory gc " +
            "WHERE (:name IS NULL OR gc.name LIKE CONCAT('%', :name, '%')) " +
            "AND (:description IS NULL OR gc.description LIKE CONCAT('%', :description, '%')) " +
            "AND (:status IS NULL OR gc.status = :status) ")
    long countGroupCategoriesBySearchConditions(
            @Param("name") String name,
            @Param("description") String description,
            @Param("status") GroupCategoryStatus status);
}