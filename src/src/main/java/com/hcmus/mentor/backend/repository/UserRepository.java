package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.domain.constant.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

    long countByStatus(Boolean status);

    long countByIdInAndStatus(List<String> userIds, Boolean status);

    long countByCreatedDateBetween(Date start, Date end);

    long countByIdInAndCreatedDateBetween(List<String> userIds, Date start, Date end);

    Boolean existsByEmail(String email);

    Boolean existsByEmailAndRolesContains(String email, UserRole role);

    Optional<User> findByEmail(String email);

    List<User> findByEmailIn(List<String> emails);

    Page<User> findByEmailLikeIgnoreCase(String email, Pageable pageable);

    List<User> findByEmailLikeIgnoreCase(String email);

    List<User> findByIdIn(List<String> ids);

    List<User> findAllByIdIn(List<String> ids);

//    List<User> findAllByRolesIn(List<String> rolesIds);

    @org.jetbrains.annotations.NotNull
    Optional<User> findById(String id);

    @Query("SELECT u FROM User u WHERE u.email = ?1 OR ?1 MEMBER OF u.additionalEmails")
    Optional<User> findByAdditionalEmailsContains(String email);

    @Query("SELECT id, name, imageUrl FROM User WHERE id = ?1")
    Optional<User> findShortProfile(String id);
}