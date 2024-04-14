package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.GroupUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupUserRepository extends JpaRepository<GroupUser, String> {
   Optional<GroupUser> findByUserIdAndGroupId(String userId, String groupId);

   boolean existsByUserIdAndGroupId(String userId, String groupId);

   @Query("SELECT COUNT(gu) > 0 FROM GroupUser gu WHERE gu.user.email = :email AND gu.group.id = :groupId AND gu.isMentor = true")
   boolean existsMentorByEmailAndGroupId(@Param("email") String email, @Param("groupId") String groupId, @Param("isMentor") boolean isMentor);

    @Query("SELECT COUNT(gu) > 0 FROM GroupUser gu WHERE gu.user.email = :email AND gu.group.id = :groupId")
    boolean existsMemberByEmailAndGroupId(@Param("email") String email, @Param("groupId") String groupId);

    void deleteByGroupIdAndUserId(String groupId, String userId);

    void deleteByUserId(String userId);
    void deleteByUserIdIn(List<String> userIds);
}
