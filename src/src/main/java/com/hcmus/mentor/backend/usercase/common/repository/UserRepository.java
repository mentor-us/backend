package com.hcmus.mentor.backend.usercase.common.repository;

import com.hcmus.mentor.backend.entity.User;
import com.hcmus.mentor.backend.payload.response.users.ProfileResponse;
import com.hcmus.mentor.backend.payload.response.users.ShortProfile;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

  Optional<User> findByEmail(String email);

  Page<User> findByEmailLikeIgnoreCase(String email, Pageable pageable);

  List<User> findByEmailLikeIgnoreCase(String email);

  // Page<User> findByEmailOrNameLikeIgnoreCase(String email, Pageable pageable);

  Boolean existsByEmail(String email);

  Boolean existsByEmailAndRolesIn(String email, User.Role role);

  List<User> findByIdIn(List<String> ids);

  List<ProfileResponse> findAllByIdIn(List<String> ids);

  List<User> findAllByRolesIn(List<String> rolesIds);

  long countByStatus(Boolean status);

  long countByIdInAndStatus(List<String> userIds, Boolean status);

  long countByCreatedDateBetween(Date start, Date end);

  long countByIdInAndCreatedDateBetween(List<String> userIds, Date start, Date end);

  @Query(value = "{id:  ?0}", fields = "{id: 1, name: 1, imageUrl: 1}")
  ShortProfile findShortProfile(String id);

  @Query(value = "{id:  {$in: ?0}}", fields = "{id: 1, name: 1, imageUrl: 1}")
  List<ShortProfile> findByIds(List<String> senderIds);
}
