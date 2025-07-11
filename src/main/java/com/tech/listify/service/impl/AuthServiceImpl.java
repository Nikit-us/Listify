package com.tech.listify.service.impl;

import com.tech.listify.dto.userdto.JwtResponseDto;
import com.tech.listify.dto.userdto.LoginRequestDto;
import com.tech.listify.dto.userdto.UserRegistrationDto;
import com.tech.listify.dto.userdto.UserResponseDto;
import com.tech.listify.exception.ResourceNotFoundException;
import com.tech.listify.exception.UserAlreadyExistsException;
import com.tech.listify.mapper.UserMapper;
import com.tech.listify.model.City;
import com.tech.listify.model.Role;
import com.tech.listify.model.User;
import com.tech.listify.model.enums.RoleType;
import com.tech.listify.repository.CityRepository;
import com.tech.listify.repository.RoleRepository;
import com.tech.listify.repository.UserRepository;
import com.tech.listify.service.AuthService;
import com.tech.listify.security.jwt.JwtTokenProvider;
import com.tech.listify.service.FileStorageService;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private final FileStorageService localFileStorageService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final CityRepository cityRepository;

    @Override
    @Transactional
    public UserResponseDto register(UserRegistrationDto registrationDto, MultipartFile avatarFile) throws IOException {
        log.info("Attempting to register new user with email: {}", registrationDto.email());

        if (userRepository.existsByEmail(registrationDto.email())) {
            log.warn("Registration failed: Email {} already exists.", registrationDto.email());
            throw new UserAlreadyExistsException("Пользователь с email '" + registrationDto.email() + "' уже существует.");
        }

        if(userRepository.existsByPhoneNumber(registrationDto.phoneNumber())) {
            log.warn("Registration failed: Phone number {} already exists.", registrationDto.phoneNumber());
            throw new UserAlreadyExistsException("Пользователь с телефоном '" + registrationDto.phoneNumber() + "' уже существует.");
        }

        User newUser = userMapper.toUser(registrationDto);

        City city = cityRepository.findById(registrationDto.cityId()).orElseThrow(() -> new ResourceNotFoundException("Город с id: " + registrationDto.cityId() + " не найден"));
        newUser.setCity(city);
        newUser.setPasswordHash(passwordEncoder.encode(registrationDto.password()));
        log.debug("Password encoded for user: {}", newUser.getEmail());

        Role userRole = roleRepository.findByName(RoleType.ROLE_USER).orElseThrow(() -> {
            log.error("Default role '{}' not found in the database!", RoleType.ROLE_USER);
            return new IllegalStateException("Ошибка конфигурации: Роль по умолчанию не найдена.");
        });
        newUser.setRoles(Set.of(userRole));
        log.debug("Assigned role '{}' to user: {}", RoleType.ROLE_USER, newUser.getEmail());

        if (avatarFile != null && !avatarFile.isEmpty()) {
            log.debug("Processing avatar for new user: {}", registrationDto.email());
            try {
                String avatarUrl = localFileStorageService.saveFile(avatarFile, "avatar");
                newUser.setAvatarUrl(avatarUrl);
                log.info("Avatar saved for new user {}. URL: {}", registrationDto.email(), avatarUrl);
            } catch (IOException e) {
                log.error("Failed to save avatar for new user {}: {}", registrationDto.email(), e.getMessage(), e);
                // Решить: откатывать регистрацию или регистрировать без аватара?
                throw new IOException("Ошибка при сохранении аватара: " + e.getMessage(), e);
            }
        }

        User savedUser = userRepository.save(newUser);
        log.info("Successfully registered new user with ID: {} and Email: {}", savedUser.getId(), savedUser.getEmail());
        return userMapper.toUserResponseDto(savedUser);
    }

    @Override
    public JwtResponseDto login(LoginRequestDto loginRequestDto) {
        log.info("Attempting login for user: {}", loginRequestDto.email());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.email(),
                        loginRequestDto.password()
                )
        );

        log.debug("Authentication successful for user: {}", loginRequestDto.email());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwtToken = jwtTokenProvider.generateToken(authentication);
        log.debug("JWT token generated for user: {}", loginRequestDto.email());

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        User user = userRepository.findByEmailWithRoles(userDetails.getUsername()).orElseThrow(
                () -> new IllegalStateException("Authenticated user not found in database - data inconsistency")
        );

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return new JwtResponseDto(jwtToken, "Bearer", user.getId(), user.getEmail(), roles);
    }
}
