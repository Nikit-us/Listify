package com.tech.listify.service.impl;

import com.tech.listify.dto.userdto.JwtResponseDto;
import com.tech.listify.dto.userdto.LoginRequestDto;
import com.tech.listify.dto.userdto.UserRegistrationDto;
import com.tech.listify.dto.userdto.UserResponseDto;
import com.tech.listify.exception.UserAlreadyExistsException;
import com.tech.listify.mapper.UserMapper;
import com.tech.listify.model.Role;
import com.tech.listify.model.User;
import com.tech.listify.model.enums.RoleType;
import com.tech.listify.repository.RoleRepository;
import com.tech.listify.repository.UserRepository;
import com.tech.listify.security.jwt.JwtTokenProvider;
import com.tech.listify.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserRegistrationDto registrationDto;
    private User user;
    private Role userRole;
    private UserResponseDto userResponseDto;

    @BeforeEach
    void setUp() {
        registrationDto = new UserRegistrationDto("John Doe", "test@example.com", "password123", "1234567890", 1);
        user = new User();
        user.setId(1L);
        user.setEmail(registrationDto.email());
        user.setFullName(registrationDto.fullName());
        userRole = new Role();
        userRole.setName(RoleType.ROLE_USER);
        userResponseDto = new UserResponseDto(1L, "test@example.com", "John Doe", "1234567890", null, null);
    }

    @Test
    void register_shouldSucceed_whenDataIsValidAndNoAvatar() throws IOException {
        when(userRepository.existsByEmail(registrationDto.email())).thenReturn(false);
        when(userMapper.toUser(registrationDto)).thenReturn(user);
        when(passwordEncoder.encode(registrationDto.password())).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleType.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserResponseDto(user)).thenReturn(userResponseDto);

        UserResponseDto result = authService.register(registrationDto, null);

        assertNotNull(result);
        assertEquals(registrationDto.email(), result.email());
        verify(userRepository).save(any(User.class));
        verify(fileStorageService, never()).saveFile(any(), any());
    }

    @Test
    void register_shouldSucceed_withAvatar() throws IOException {
        MultipartFile avatarFile = new MockMultipartFile("avatar", "avatar.jpg", "image/jpeg", "some-image-bytes".getBytes());
        String avatarUrl = "/uploads/avatars/some-uuid.jpg";

        when(userRepository.existsByEmail(registrationDto.email())).thenReturn(false);
        when(userMapper.toUser(registrationDto)).thenReturn(user);
        when(passwordEncoder.encode(registrationDto.password())).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleType.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(fileStorageService.saveFile(avatarFile, "avatar")).thenReturn(avatarUrl);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserResponseDto(user)).thenReturn(userResponseDto);

        authService.register(registrationDto, avatarFile);

        verify(fileStorageService, times(1)).saveFile(avatarFile, "avatar");
        verify(userRepository).save(user);
        assertEquals(avatarUrl, user.getAvatarUrl());
    }

    @Test
    void register_shouldThrowException_whenEmailExists() {
        when(userRepository.existsByEmail(registrationDto.email())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(registrationDto, null));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_shouldReturnJwt_whenCredentialsAreValid() {
        LoginRequestDto loginDto = new LoginRequestDto("test@example.com", "password123");
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(userRepository.findByEmailWithRoles(loginDto.email())).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("test.jwt.token");

        JwtResponseDto result = authService.login(loginDto);

        assertNotNull(result);
        assertEquals("test.jwt.token", result.token());
        assertEquals("Bearer", result.type());
        assertEquals(user.getId(), result.userId());
        verify(jwtTokenProvider, times(1)).generateToken(authentication);
    }

    @Test
    void login_shouldThrowException_whenCredentialsAreInvalid() {
        LoginRequestDto loginDto = new LoginRequestDto("test@example.com", "wrongpassword");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(loginDto));
        verify(jwtTokenProvider, never()).generateToken(any());
    }
}