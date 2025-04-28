package com.tech.listify.service.impl;

import com.tech.listify.dto.JwtResponseDto;
import com.tech.listify.dto.LoginRequestDto;
import com.tech.listify.dto.UserRegistrationDto;
import com.tech.listify.dto.UserResponseDto;
import com.tech.listify.exception.UserAlreadyExistsException;
import com.tech.listify.mapper.UserMapper;
import com.tech.listify.model.Role;
import com.tech.listify.model.User;
import com.tech.listify.model.enums.RoleType;
import com.tech.listify.repository.RoleRepository;
import com.tech.listify.repository.UserRepository;
import com.tech.listify.service.AuthService;
import com.tech.listify.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public UserResponseDto register(UserRegistrationDto registrationDto) {
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
        return userMapper.toUserResponseDto(savedUser);
    }

    @Override
    public JwtResponseDto login(LoginRequestDto loginRequestDto) {
        log.info("Attempting login for user: {}", loginRequestDto.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getEmail(),
                        loginRequestDto.getPassword()
                )
        );

        log.debug("Authentication successful for user: {}", loginRequestDto.getEmail());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwtToken = jwtTokenProvider.generateToken(authentication);
        log.debug("JWT token generated for user: {}", loginRequestDto.getEmail());

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow(
                () -> new IllegalStateException("Authenticated user not found in database - data inconsistency")
        );

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return new JwtResponseDto(jwtToken, user.getId(), user.getEmail(), roles);
    }
}
