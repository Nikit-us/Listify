package com.tech.listify.service.impl;

import com.tech.listify.dto.UserRegistrationDto;
import com.tech.listify.exception.UserAlreadyExistsException;
import com.tech.listify.mapper.UserMapper;
import com.tech.listify.model.Role;
import com.tech.listify.model.User;
import com.tech.listify.model.enums.RoleType;
import com.tech.listify.repository.RoleRepository;
import com.tech.listify.repository.UserRepository;
import com.tech.listify.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public User registerNewUser(UserRegistrationDto registrationDto) {
        log.info("Attempting to register new user with email: {}", registrationDto.getEmail());

        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            log.warn("Registration failed: Email {} already exists.", registrationDto.getEmail());
            throw new UserAlreadyExistsException("Пользователь с email '" + registrationDto.getEmail() + "' уже существует.");
        }

        User newUser = userMapper.toUser(registrationDto);

        newUser.setPasswordHash(passwordEncoder.encode(registrationDto.getPassword()));
        log.debug("Password encoded for user: {}", newUser.getEmail());

        Role userRole = roleRepository.findByName(RoleType.ROLE_USER).orElseThrow(() -> {
            log.error("Default role '{}' not found in the database!", RoleType.ROLE_USER);
            return new IllegalStateException("Ошибка конфигурации: Роль по умолчанию не найдена.");
        });
        newUser.setRoles(Set.of(userRole));
        log.debug("Assigned role '{}' to user: {}", RoleType.ROLE_USER, newUser.getEmail());

        User savedUser = userRepository.save(newUser);
        log.info("Successfully registered new user with ID: {} and Email: {}", savedUser.getId(), savedUser.getEmail());
        return savedUser;
    }
}
