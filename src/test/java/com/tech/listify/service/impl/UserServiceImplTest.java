package com.tech.listify.service.impl;

import com.tech.listify.dto.userdto.UserProfileDto;
import com.tech.listify.dto.userdto.UserUpdateProfileDto;
import com.tech.listify.exception.ResourceNotFoundException;
import com.tech.listify.mapper.UserMapper;
import com.tech.listify.model.City;
import com.tech.listify.model.User;
import com.tech.listify.model.enums.AdvertisementStatus;
import com.tech.listify.repository.AdvertisementRepository;
import com.tech.listify.repository.UserRepository;
import com.tech.listify.service.CityService;
import com.tech.listify.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CityService cityService;
    @Mock
    private AdvertisementRepository advertisementRepository;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private final String userEmail = "user@example.com";

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail(userEmail);
        user.setFullName("Old Name");
    }

    @Test
    void getUserProfileById_shouldReturnProfile_whenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(advertisementRepository.countBySellerIdAndStatus(1L, AdvertisementStatus.ACTIVE)).thenReturn(5);
        when(userMapper.toUserProfileDto(user, 5)).thenReturn(new UserProfileDto(1L, "Old Name", userEmail, null, null, null, null, 5, null));

        UserProfileDto result = userService.getUserProfileById(1L);

        assertNotNull(result);
        assertEquals(5, result.totalActiveAdvertisements());
        assertEquals("Old Name", result.fullName());
    }

    @Test
    void getUserProfileById_shouldThrowNotFound_whenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserProfileById(99L));
    }

    @Test
    void updateUserProfile_shouldUpdateNameAndCity() throws IOException {
        UserUpdateProfileDto updateDto = new UserUpdateProfileDto("New Name", null, 10);
        City newCity = new City();
        newCity.setId(10);
        newCity.setName("New City");

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(cityService.findCityById(10)).thenReturn(newCity);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserProfileDto(any(), anyInt())).thenReturn(new UserProfileDto(1L, "New Name", userEmail, 10, "New City", null, null, 0, null));

        userService.updateUserProfile(userEmail, updateDto, null);

        verify(userRepository).save(user);
        assertEquals("New Name", user.getFullName());
        assertEquals(newCity, user.getCity());
    }

    @Test
    void updateUserProfile_shouldUpdateAvatar() throws IOException {
        // Given
        UserUpdateProfileDto updateDto = new UserUpdateProfileDto(null, null, null);
        MockMultipartFile avatarFile = new MockMultipartFile("avatar", "new.jpg", "image/jpeg", "data".getBytes());
        String newAvatarUrl = "/uploads/avatars/new-uuid.jpg";

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(fileStorageService.saveFile(avatarFile, "avatar")).thenReturn(newAvatarUrl);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserProfileDto(any(), anyInt())).thenReturn(new UserProfileDto(1L, "Old Name", userEmail, null, null, null, null, 0, newAvatarUrl));

        userService.updateUserProfile(userEmail, updateDto, avatarFile);

        verify(userRepository).save(user);
        assertEquals(newAvatarUrl, user.getAvatarUrl());
    }

    @Test
    void updateUserProfile_shouldDoNothing_whenDtoAndFileAreNull() throws IOException {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        userService.updateUserProfile(userEmail, null, null);

        verify(userRepository, never()).save(any());
    }
}