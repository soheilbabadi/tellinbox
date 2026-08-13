package com.tellinbox.tellinbox_api.security;

import com.tellinbox.tellinbox_api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;

/**
 * Custom UserDetailsService implementation that loads user data from the database.
 * Uses UserRepository to fetch user details by mobile, email, or username.
 * 
 * @author Tellinbox Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {


    private final UserRepository userRepository;
    private final MessageSource messageSource;

    /**
     * Loads user by username (can be mobile, email, or username).
     * Tries to find user by each identifier in order.
     * 
     * @param identifier username, mobile, or email
     * @return UserDetails object
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        return userRepository.findByMobile(identifier)
            .or(() -> userRepository.findByEmail(identifier))
            .or(() -> userRepository.findByUsername(identifier))
            .map(user -> {
                if (user.getIsDeleted() || !user.isActive()) {
                    throw new UsernameNotFoundException(getMessage("error.UsernameNotFoundException.user_account_is_deactivated_or_deleted"));
                }
                
                return CustomUserDetails.create(
                    user.getId(),
                    user.getMobile(), // Use mobile as username for security
                    user.getPasswordHash() != null ? user.getPasswordHash() : "",
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                );
            })
            .orElseThrow(() -> new UsernameNotFoundException(getMessage("error.UsernameNotFoundException.user_not_found_with_identifier", identifier)));
    }

    /**
     * Loads user by UUID.
     * 
     * @param userId user's UUID
     * @return UserDetails object
     * @throws UsernameNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserByUuid(UUID userId) throws UsernameNotFoundException {
        return userRepository.findById(userId)
            .filter(user -> !user.getIsDeleted() && user.isActive())
            .map(user -> CustomUserDetails.create(
                user.getId(),
                user.getMobile(),
                user.getPasswordHash() != null ? user.getPasswordHash() : "",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
            ))
            .orElseThrow(() -> new UsernameNotFoundException(getMessage("error.UsernameNotFoundException.user_not_found_with_id", userId)));
    }

    /**
     * Checks if user exists by identifier.
     * 
     * @param identifier username, mobile, or email
     * @return true if user exists and is active
     */
    @Transactional(readOnly = true)
    public boolean userExists(String identifier) {
        return userRepository.findByMobile(identifier)
            .or(() -> userRepository.findByEmail(identifier))
            .or(() -> userRepository.findByUsername(identifier))
            .filter(user -> !user.getIsDeleted() && user.isActive())
            .isPresent();
    }

    protected String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, java.util.Locale.forLanguageTag("fa"));
    }

    }
