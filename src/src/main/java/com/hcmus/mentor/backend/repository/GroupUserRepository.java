package com.hcmus.mentor.backend.repository;

import com.hcmus.mentor.backend.domain.GroupUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupUserRepository extends JpaRepository<GroupUser, String> {
}
