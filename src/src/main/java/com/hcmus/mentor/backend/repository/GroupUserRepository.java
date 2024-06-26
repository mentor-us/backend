package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.GroupUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupUserRepository extends JpaRepository<GroupUser, String> {

    void deleteByGroupIdAndUserId(String groupId, String userId);

    void deleteByUserId(String userId);

    void deleteByUserIdIn(List<String> userIds);

    boolean existsByUserIdAndGroupId(String userId, String groupId);

    Optional<GroupUser> findByUserIdAndGroupId(String userId, String groupId);

    @Query("SELECT COUNT(gu) > 0 FROM GroupUser gu WHERE gu.user.email = :email AND gu.group.id = :groupId AND gu.isMentor = :isMentor")
    boolean existsMentorByEmailAndGroupId(@Param("email") String email, @Param("groupId") String groupId, @Param("isMentor") boolean isMentor);

    @Query("SELECT COUNT(gu) > 0 FROM GroupUser gu WHERE gu.user.email = :email AND gu.group.id = :groupId")
    boolean existsMemberByEmailAndGroupId(@Param("email") String email, @Param("groupId") String groupId);

    @Query("select g from Group g inner join g.groupUsers gu where g.id = ?1 and gu.id = ?2")
    boolean existsUserInGroup(String groupId, String userId);
}