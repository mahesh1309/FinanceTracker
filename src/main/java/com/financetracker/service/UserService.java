package com.financetracker.service;

import com.financetracker.model.User;
import com.financetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Authenticated user not found in database: " + email));
    }

    public List<User> searchUsers(String emailFragment, String excludeEmail) {
        if (emailFragment == null || emailFragment.trim().length() < 2) {
            return List.of();
        }
        String escaped = Pattern.quote(emailFragment.trim());
        return userRepository.searchByEmailFragment(escaped, excludeEmail);
    }
}
