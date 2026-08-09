package com.tellinbox.tellinbox_api.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

/**
 * Custom UserDetails implementation that includes user UUID.
 * This allows accessing the user's UUID from @AuthenticationPrincipal.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final UUID userId;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;
    private final boolean accountNonExpired;
    private final boolean accountNonLocked;
    private final boolean credentialsNonExpired;

    /**
     * Create CustomUserDetails from user data.
     */
    public static CustomUserDetails create(UUID userId, String username, String password, 
                                           Collection<? extends GrantedAuthority> authorities) {
        return new CustomUserDetails(
            userId,
            username,
            password != null ? password : "",
            authorities,
            true, // enabled
            true, // accountNonExpired
            true, // accountNonLocked
            true  // credentialsNonExpired
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
