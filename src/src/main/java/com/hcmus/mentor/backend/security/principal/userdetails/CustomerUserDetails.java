package com.hcmus.mentor.backend.security.principal.userdetails;

import com.hcmus.mentor.backend.domain.User;
import com.hcmus.mentor.backend.domain.constant.UserRole;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

@ToString
public class CustomerUserDetails implements OAuth2User, OidcUser, UserDetails {
    @Getter
    private final String id;

    @Getter
    private final String email;

    @Getter
    private final String password;

    @Getter
    List<UserRole> roles;

    @Getter
    private final Collection<? extends GrantedAuthority> authorities;

    @Getter
    @Setter
    public Map<String, Object> attributes;

    private OidcUserInfo oidcUserInfo;
    private OidcIdToken oidcIdToken;

    public CustomerUserDetails(
            String id,
            String email,
            String password,
            List<UserRole> roles,
            Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.authorities = authorities;
    }

    public static CustomerUserDetails create(User user, Map<String, Object> attributes) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        var roles = user.getRoles();
        for (var role : roles) {
            authorities.add(new SimpleGrantedAuthority(String.format("ROLE_%s", role).toUpperCase()));
        }

        var userDetails = new CustomerUserDetails(user.getId(), user.getEmail(), user.getPassword(), roles, authorities);
        userDetails.setAttributes(attributes);

        return userDetails;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getName() {
        return String.valueOf(id);
    }

    @Override
    public Map<String, Object> getClaims() {
        return Collections.emptyMap();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return oidcUserInfo;
    }

    @Override
    public OidcIdToken getIdToken() {
        return oidcIdToken;
    }
}
