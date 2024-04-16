package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.controller.payload.response.users.ProfileResponse;
import com.hcmus.mentor.backend.controller.payload.response.users.ShortProfile;
import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.domain.constant.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    List<User> findByEmailIn(List<String> emails);

    @Query("SELECT u FROM User u WHERE u.email = ?1 OR ?1 MEMBER OF u.additionalEmails")
    Optional<User> findByAdditionalEmailsContains(String email);

    Page<User> findByEmailLikeIgnoreCase(String email, Pageable pageable);

    List<User> findByEmailLikeIgnoreCase(String email);

    // Page<User> findByEmailOrNameLikeIgnoreCase(String email, Pageable pageable);

    Boolean existsByEmail(String email);

    Boolean existsByEmailAndRolesContains(String email, UserRole role);

    List<User> findByIdIn(List<String> ids);

    List<ProfileResponse> findAllByIdIn(List<String> ids);

    List<User> findAllByRolesIn(List<String> rolesIds);

    long countByStatus(Boolean status);

    long countByIdInAndStatus(List<String> userIds, Boolean status);

    long countByCreatedDateBetween(Date start, Date end);

    long countByIdInAndCreatedDateBetween(List<String> userIds, Date start, Date end);

    Optional<User> findById(String id);

    //    @Query(value = "{id:  ?0}", fields = "{id: 1, name: 1, imageUrl: 1}")
    // TODO: Fix this
    @Query("SELECT id, name, imageUrl FROM User WHERE id = ?1")
    ShortProfile findShortProfile(String id);

//    @Query(value = "{id:  {$in: ?0}}", fields = "{id: 1, name: 1, imageUrl: 1}")
    // TODO: Fix this
//    List<ShortProfile> findByIds(List<String> senderIds);


}
