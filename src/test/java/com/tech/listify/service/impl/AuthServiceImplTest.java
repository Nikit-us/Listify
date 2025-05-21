package com.tech.listify.service.impl; // Убедись, что пакет совпадает

import com.tech.listify.dto.userDto.JwtResponseDto;
import com.tech.listify.dto.userDto.LoginRequestDto;
import com.tech.listify.dto.userDto.UserRegistrationDto;
import com.tech.listify.dto.userDto.UserResponseDto;
import com.tech.listify.exception.UserAlreadyExistsException;
import com.tech.listify.mapper.UserMapper;
import com.tech.listify.model.Role;
import com.tech.listify.model.User;
import com.tech.listify.model.enums.RoleType;
import com.tech.listify.repository.RoleRepository;
import com.tech.listify.repository.UserRepository;
import com.tech.listify.security.jwt.JwtTokenProvider; // Твой JwtTokenProvider
// import com.tech.listify.service.FileStorageService; // Можно использовать интерфейс для мока
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException; // Для теста неудачного логина
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
    private LocalFileStorageServiceImpl localFileStorageService; // Мокаем конкретную реализацию
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private MultipartFile mockAvatarFile;
    @Mock
    private Authentication mockAuthentication; // Для мокирования результата authenticationManager.authenticate

    @InjectMocks
    private AuthServiceImpl authService;

    private UserRegistrationDto registrationDto;
    private User mappedUserFromDto; // Пользователь, возвращаемый userMapper.toUser()
    private Role userRoleEntity;    // Сущность Role
    private User savedUserEntity;   // Пользователь, возвращаемый userRepository.save()
    private UserResponseDto userResponseDtoAfterSave; // DTO, возвращаемый userMapper.toUserResponseDto()

    @BeforeEach
    void setUp() {
        registrationDto = new UserRegistrationDto();
        registrationDto.setEmail("test@example.com");
        registrationDto.setPassword("password123");
        registrationDto.setFullName("Test User");
        registrationDto.setPhoneNumber("123456");

        userRoleEntity = new Role();
        userRoleEntity.setId(1);
        userRoleEntity.setName(RoleType.ROLE_USER); // Устанавливаем Enum

        mappedUserFromDto = new User();
        mappedUserFromDto.setEmail(registrationDto.getEmail());
        mappedUserFromDto.setFullName(registrationDto.getFullName());
        mappedUserFromDto.setPhoneNumber(registrationDto.getPhoneNumber());
        // isActive по умолчанию true в User

        savedUserEntity = new User();
        savedUserEntity.setId(1L);
        savedUserEntity.setEmail(registrationDto.getEmail());
        savedUserEntity.setFullName(registrationDto.getFullName());
        savedUserEntity.setPhoneNumber(registrationDto.getPhoneNumber());
        savedUserEntity.setPasswordHash("encodedPassword"); // Устанавливаем после passwordEncoder.encode
        savedUserEntity.setRoles(Set.of(userRoleEntity)); // Устанавливаем после roleRepository.findByName
        savedUserEntity.setIsActive(true);
        savedUserEntity.setRegisteredAt(OffsetDateTime.now());

        userResponseDtoAfterSave = new UserResponseDto();
        userResponseDtoAfterSave.setId(1L);
        userResponseDtoAfterSave.setEmail(registrationDto.getEmail());
        userResponseDtoAfterSave.setFullName(registrationDto.getFullName());
        userResponseDtoAfterSave.setPhoneNumber(registrationDto.getPhoneNumber());
        userResponseDtoAfterSave.setRegisteredAt(savedUserEntity.getRegisteredAt());
    }

    @Test
    @DisplayName("register - новый пользователь без аватара - успешная регистрация")
    void register_whenNewUserWithoutAvatar_shouldRegisterSuccessfully() throws IOException {
        // Arrange
        when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(false);
        when(userMapper.toUser(registrationDto)).thenReturn(mappedUserFromDto);
        when(passwordEncoder.encode(registrationDto.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleType.ROLE_USER)).thenReturn(Optional.of(userRoleEntity));
        when(userRepository.save(any(User.class))).thenReturn(savedUserEntity); // Возвращаем подготовленный savedUserEntity
        when(userMapper.toUserResponseDto(savedUserEntity)).thenReturn(userResponseDtoAfterSave);

        // Act
        UserResponseDto result = authService.register(registrationDto, null); // avatarFile is null

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(registrationDto.getEmail());
        assertThat(result.getFullName()).isEqualTo(registrationDto.getFullName());

        verify(userRepository).existsByEmail(registrationDto.getEmail());
        verify(userMapper).toUser(registrationDto);
        verify(passwordEncoder).encode(registrationDto.getPassword());
        verify(roleRepository).findByName(RoleType.ROLE_USER);
        // Проверяем, что в newUser были установлены пароль и роли перед сохранением
        verify(userRepository).save(argThat(userToSave ->
                userToSave.getEmail().equals(registrationDto.getEmail()) &&
                        "encodedPassword".equals(userToSave.getPasswordHash()) &&
                        userToSave.getRoles().contains(userRoleEntity) &&
                        userToSave.getAvatarUrl() == null // Аватар не должен быть установлен
        ));
        verify(localFileStorageService, never()).saveFile(any(), anyString()); // saveFile не вызывался
        verify(userMapper).toUserResponseDto(savedUserEntity);
    }

    @Test
    @DisplayName("register - новый пользователь с аватаром - успешная регистрация")
    void register_whenNewUserWithAvatar_shouldRegisterSuccessfully() throws IOException {
        // Arrange
        String avatarUrl = "/uploads/avatars/avatar-uuid.jpg";
        when(mockAvatarFile.isEmpty()).thenReturn(false); // Файл не пустой
        when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(false);
        when(userMapper.toUser(registrationDto)).thenReturn(mappedUserFromDto);
        when(passwordEncoder.encode(registrationDto.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleType.ROLE_USER)).thenReturn(Optional.of(userRoleEntity));
        when(localFileStorageService.saveFile(mockAvatarFile, "avatar")).thenReturn(avatarUrl);

        // Настраиваем userRepository.save, чтобы он возвращал юзера с установленным URL аватара
        savedUserEntity.setAvatarUrl(avatarUrl); // Обновляем наш эталонный savedUserEntity
        userResponseDtoAfterSave.setAvatarUrl(avatarUrl); // И DTO для ответа
        when(userRepository.save(any(User.class))).thenReturn(savedUserEntity);
        when(userMapper.toUserResponseDto(savedUserEntity)).thenReturn(userResponseDtoAfterSave);


        // Act
        UserResponseDto result = authService.register(registrationDto, mockAvatarFile);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAvatarUrl()).isEqualTo(avatarUrl);

        verify(localFileStorageService).saveFile(mockAvatarFile, "avatar");
        verify(userRepository).save(argThat(userToSave ->
                avatarUrl.equals(userToSave.getAvatarUrl()) // Проверяем, что URL аватара установлен
        ));
        verify(userMapper).toUserResponseDto(savedUserEntity);
    }

    @Test
    @DisplayName("register - пользователь уже существует - UserAlreadyExistsException")
    void register_whenUserAlreadyExists_shouldThrowUserAlreadyExistsException() throws IOException {
        // Arrange
        when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(registrationDto, null))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Пользователь с email '" + registrationDto.getEmail() + "' уже существует.");

        verify(localFileStorageService, never()).saveFile(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("register - роль по умолчанию не найдена - IllegalStateException")
    void register_whenDefaultRoleNotFound_shouldThrowIllegalStateException() {
        // Arrange
        when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(false);
        when(userMapper.toUser(registrationDto)).thenReturn(mappedUserFromDto);
        when(passwordEncoder.encode(registrationDto.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleType.ROLE_USER)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.register(registrationDto, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Ошибка конфигурации: Роль по умолчанию не найдена.");
    }

    @Test
    @DisplayName("register - ошибка сохранения аватара - IOException и откат")
    void register_whenAvatarSaveFails_shouldThrowIOException() throws IOException {
        // Arrange
        when(mockAvatarFile.isEmpty()).thenReturn(false);
        when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(false);
        when(userMapper.toUser(registrationDto)).thenReturn(mappedUserFromDto);
        when(passwordEncoder.encode(registrationDto.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName(RoleType.ROLE_USER)).thenReturn(Optional.of(userRoleEntity));
        when(localFileStorageService.saveFile(mockAvatarFile, "avatar")).thenThrow(new IOException("Disk full"));

        // Act & Assert
        assertThatThrownBy(() -> authService.register(registrationDto, mockAvatarFile))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Ошибка при сохранении аватара: Disk full");

        verify(userRepository, never()).save(any()); // Проверяем, что пользователь не сохранился
    }

    @Test
    @DisplayName("login - корректные данные - успешный вход и возврат JwtResponseDto")
    void login_whenCredentialsAreValid_shouldReturnJwtResponseDto() {
        // Arrange
        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("password123");

        // UserDetails, который будет возвращен из Authentication principal
        // Мы используем наш User, так как он реализует UserDetails
        // Роли должны быть уже в нем (FetchType.EAGER для User.roles)
        org.springframework.security.core.userdetails.User springUserDetails =
                new org.springframework.security.core.userdetails.User(
                        savedUserEntity.getEmail(),
                        savedUserEntity.getPasswordHash(),
                        savedUserEntity.getAuthorities() // Используем getAuthorities из нашей User сущности
                );

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication); // Мокируем Authentication
        when(mockAuthentication.getPrincipal()).thenReturn(springUserDetails); // Мокируем Principal
        when(jwtTokenProvider.generateToken(mockAuthentication)).thenReturn("mocked.jwt.token");
        // Мокируем поиск пользователя после аутентификации для получения ID и других данных
        when(userRepository.findByEmail(springUserDetails.getUsername())).thenReturn(Optional.of(savedUserEntity));

        List<String> expectedRoles = savedUserEntity.getRoles().stream().map(role -> role.getName().name()).toList();

        // Act
        JwtResponseDto result = authService.login(loginDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("mocked.jwt.token");
        assertThat(result.getUserId()).isEqualTo(savedUserEntity.getId());
        assertThat(result.getEmail()).isEqualTo(savedUserEntity.getEmail());
        assertThat(result.getRoles()).containsExactlyInAnyOrderElementsOf(expectedRoles);

        verify(authenticationManager).authenticate(
                argThat(token -> token.getName().equals(loginDto.getEmail()) &&
                        token.getCredentials().toString().equals(loginDto.getPassword()))
        );
        verify(jwtTokenProvider).generateToken(mockAuthentication);
        verify(userRepository).findByEmail(savedUserEntity.getEmail());
    }

    @Test
    @DisplayName("login - неверные учетные данные - AuthenticationException (BadCredentialsException)")
    void login_whenCredentialsAreInvalid_shouldThrowAuthenticationException() {
        // Arrange
        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("wrongpassword");

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials")); // Имитируем ошибку аутентификации

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginDto))
                .isInstanceOf(AuthenticationException.class); // или BadCredentialsException.class

        verify(jwtTokenProvider, never()).generateToken(any());
        // Убираем проверку SecurityContextHolder, так как она не нужна для этого сценария
        // verify(SecurityContextHolder.getContext(), never()).setAuthentication(any());
    }

    @Test
    @DisplayName("login - аутентифицированный пользователь не найден в БД - IllegalStateException")
    void login_whenAuthenticatedUserNotFoundInDb_shouldThrowIllegalStateException() {
        // Arrange
        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("password123");

        org.springframework.security.core.userdetails.User springUserDetails =
                new org.springframework.security.core.userdetails.User(
                        "test@example.com", "password", Collections.emptyList());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);
        when(mockAuthentication.getPrincipal()).thenReturn(springUserDetails);
        when(jwtTokenProvider.generateToken(mockAuthentication)).thenReturn("mocked.jwt.token");
        // Имитируем, что пользователь не найден ПОСЛЕ успешной аутентификации
        when(userRepository.findByEmail(springUserDetails.getUsername())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Authenticated user not found in database");
    }
}