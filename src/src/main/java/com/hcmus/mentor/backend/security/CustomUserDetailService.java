package com.hcmus.mentor.backend.security;

import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user =
                userRepository
                        .findByEmail(username)
                        .orElseThrow(
                                () ->
                                        new UsernameNotFoundException(
                                                String.format("User not found with email: %s", username)));

        List<GrantedAuthority> authorities =
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        return new UserPrincipal(user.getId(), user.getEmail(), user.getPassword(), authorities);
    }
}
