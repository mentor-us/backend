package com.hcmus.mentor.backend.security;

import com.hcmus.mentor.backend.entity.User;
import com.hcmus.mentor.backend.exception.ResourceNotFoundException;
import com.hcmus.mentor.backend.repository.UserRepository;
import com.hcmus.mentor.backend.service.PermissionService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Created by rajeevkumarsingh on 02/08/17. */
@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  private final PermissionService permissionService;

  public CustomUserDetailsService(
      UserRepository userRepository, PermissionService permissionService) {
    this.userRepository = userRepository;
    this.permissionService = permissionService;
  }

  @Override
  @Transactional
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () -> new UsernameNotFoundException("User not found with email : " + email));
    String role = permissionService.isAdmin(email) ? "ROLE_ADMIN" : "ROLE_USER";
    return UserPrincipal.create(user, role);
  }

  @Transactional
  public UserDetails loadUserById(String id) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    String role = permissionService.isAdmin(user.getEmail()) ? "ROLE_ADMIN" : "ROLE_USER";
    return UserPrincipal.create(user, role);
  }
}
