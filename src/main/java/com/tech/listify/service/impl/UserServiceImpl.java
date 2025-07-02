package com.tech.listify.service.impl;

import com.tech.listify.dto.userdto.UserProfileDto;
import com.tech.listify.dto.userdto.UserUpdateProfileDto;
import com.tech.listify.exception.ResourceNotFoundException;
import com.tech.listify.mapper.UserMapper;
import com.tech.listify.model.City;
import com.tech.listify.model.User;
import com.tech.listify.model.enums.AdvertisementStatus;
import com.tech.listify.repository.AdvertisementRepository;
import com.tech.listify.repository.CityRepository;
import com.tech.listify.repository.UserRepository;
import com.tech.listify.service.LocationService;
import com.tech.listify.service.FileStorageService;
import com.tech.listify.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final LocationService locationService;
    private final CityRepository cityRepository;
    private final AdvertisementRepository advertisementRepository;
    private final FileStorageService fileStorageService;
    private final UserMapper userMapper;


    @Override
    @Transactional(readOnly = true)
    public UserProfileDto getUserProfileById(Long userId) {
        log.debug("Fetching user profile for ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с ID " + userId + " не найден."));
        int activeAdsCount = advertisementRepository.countBySellerIdAndStatus(user.getId(), AdvertisementStatus.ACTIVE);
        return userMapper.toUserProfileDto(user, activeAdsCount);
    }

    @Override
    @Transactional
    public UserProfileDto updateUserProfile(String userEmail, UserUpdateProfileDto updateDto, MultipartFile avatarFile) throws IOException {
        log.info("Attempting to update profile for user: {}", userEmail);
        User user = findUserByEmailOrThrow(userEmail);
        boolean isProfileUpdated = updateProfileData(user, updateDto);
        boolean isAvatarUpdated = updateAvatar(user, avatarFile);

        if (isProfileUpdated || isAvatarUpdated) {
            userRepository.save(user);
            log.info("Profile updated for user: {}", userEmail);
        } else {
            log.info("No profile changes detected for user: {}", userEmail);
        }

        int activeAdsCount = advertisementRepository.countBySellerIdAndStatus(user.getId(), AdvertisementStatus.ACTIVE);
        return userMapper.toUserProfileDto(user, activeAdsCount);
    }

    private boolean updateProfileData(User user, UserUpdateProfileDto updateDto) {
        if (updateDto == null) {
            return false;
        }
        boolean updated = false;

        if (updateDto.fullName() != null && !updateDto.fullName().isBlank()) {
            user.setFullName(updateDto.fullName());
            updated = true;
        }
        if (updateDto.phoneNumber() != null) {
            user.setPhoneNumber(updateDto.phoneNumber().isBlank() ? null : updateDto.phoneNumber());
            updated = true;
        }
        if (updateDto.cityId() != null) {
            if (updateDto.cityId() <= 0) {
                user.setCity(null);
            } else {
                City city = cityRepository.findById(updateDto.cityId()).orElseThrow(() -> new ResourceNotFoundException("Город с id " + updateDto.cityId() + " не найден"));
                user.setCity(city);
            }
            updated = true;
        }
        return updated;
    }

    private boolean updateAvatar(User user, MultipartFile avatarFile) throws IOException {
        if (avatarFile == null || avatarFile.isEmpty()) {
            return false;
        }
        log.debug("Processing new avatar for user: {}", user.getEmail());
        String newAvatarUrl = fileStorageService.saveFile(avatarFile, "avatar");
        user.setAvatarUrl(newAvatarUrl);
        log.info("New avatar uploaded for user {}. URL: {}", user.getEmail(), newAvatarUrl);
        return true;
    }


    @Override
    public UserProfileDto getCurrentUserProfile(String userEmail) {
        log.debug("Fetching current user profile for email: {}", userEmail);
        User user = findUserByEmailOrThrow(userEmail);
        int activeAdsCount = advertisementRepository.countBySellerIdAndStatus(user.getId(), AdvertisementStatus.ACTIVE);
        return userMapper.toUserProfileDto(user, activeAdsCount);
    }

    private User findUserByEmailOrThrow(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.error("Authenticated user with email {} not found in database.", userEmail);
                    return new ResourceNotFoundException("Пользователь с email " + userEmail + " не найден.");
                });
    }
}