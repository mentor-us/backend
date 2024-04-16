package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.GroupCategory;
import com.hcmus.mentor.backend.domain.constant.GroupCategoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupCategoryRepository extends JpaRepository<GroupCategory, String> {
    Optional<GroupCategory> findById(String id);

    boolean existsByName(String name);

    GroupCategory findByName(String name);

    List<GroupCategory> findByIdIn(List<String> ids);

    List<GroupCategory> findAllByStatus(GroupCategoryStatus status);

    @Query(value = "SELECT * FROM group_category " +
            "WHERE (:name IS NULL OR name LIKE CONCAT('%', :name, '%')) " +
            "AND (:description IS NULL OR description LIKE CONCAT('%', :description, '%')) " +
            "AND (:status IS NULL OR status = :status) " +
            "ORDER BY created_date DESC " +
            "LIMIT :pageSize OFFSET :offset", nativeQuery = true)
    List<GroupCategory> findGroupCategoriesBySearchConditions(
            @Param("name") String name,
            @Param("description") String description,
            @Param("status") GroupCategoryStatus status,
            @Param("pageSize") int pageSize,
            @Param("offset") int offset);

    @Query(value = "SELECT COUNT(*) FROM group_category " +
            "WHERE (:name IS NULL OR name LIKE CONCAT('%', :name, '%')) " +
            "AND (:description IS NULL OR description LIKE CONCAT('%', :description, '%')) " +
            "AND (:status IS NULL OR status = :status)", nativeQuery = true)
    long countGroupCategoriesBySearchConditions(
            @Param("name") String name,
            @Param("description") String description,
            @Param("status") GroupCategoryStatus status);
}
