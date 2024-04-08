package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.GroupUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupUserRepository extends JpaRepository<GroupUser, String> {
}
