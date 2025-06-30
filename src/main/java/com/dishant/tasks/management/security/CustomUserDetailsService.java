
package com.dishant.tasks.management.security;

import com.dishant.tasks.management.model.User;
import com.dishant.tasks.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.dishant.tasks.management.constants.Constants.USER_NOT_FOUND_ERROR_MESSAGE;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String emailOrUsername) throws UsernameNotFoundException {
        User user = userRepository.findByEmailOrUsername(emailOrUsername, emailOrUsername)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_ERROR_MESSAGE)).getUser();

        if (!user.isEnabled()) {
            throw new UsernameNotFoundException("Email not verified. Please verify before login.");
        }

        return new CustomUserDetails(user);
    }
}

