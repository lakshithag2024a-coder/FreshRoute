package com.freshroute.service;

import com.freshroute.dto.UserProfileDto;
import com.freshroute.dto.UserRegistrationDto;
import com.freshroute.entity.User;
import com.freshroute.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(UserRegistrationDto dto) {
        if (userRepository.existsByUsername(dto.getUsername().trim())) {
            throw new IllegalArgumentException("Username '" + dto.getUsername() + "' is already taken.");
        }
        if (userRepository.existsByEmail(dto.getEmail().trim())) {
            throw new IllegalArgumentException("Email '" + dto.getEmail() + "' is already registered.");
        }

        User user = new User();
        user.setUsername(dto.getUsername().trim());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName().trim());
        user.setEmail(dto.getEmail().trim().toLowerCase());
        user.setOrganizationName(dto.getOrganizationName() != null ? dto.getOrganizationName().trim() : null);
        user.setRole(dto.getRole());
        user.setPhone(dto.getPhone().trim());
        user.setAddress(dto.getAddress().trim());
        user.setCity(dto.getCity().trim());
        user.setZipCode(dto.getZipCode() != null ? dto.getZipCode().trim() : null);
        user.setEnabled(true);

        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User getCurrentAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }

    @Transactional
    public User updateProfile(User user, UserProfileDto dto) {
        user.setFullName(dto.getFullName().trim());
        user.setEmail(dto.getEmail().trim().toLowerCase());
        user.setOrganizationName(dto.getOrganizationName() != null ? dto.getOrganizationName().trim() : null);
        user.setPhone(dto.getPhone().trim());
        user.setAddress(dto.getAddress().trim());
        user.setCity(dto.getCity().trim());
        user.setZipCode(dto.getZipCode() != null ? dto.getZipCode().trim() : null);
        return userRepository.save(user);
    }
}
