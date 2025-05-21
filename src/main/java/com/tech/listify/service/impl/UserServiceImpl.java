package com.tech.listify.service.impl;

import com.tech.listify.dto.userDto.UserProfileDto;
import com.tech.listify.dto.userDto.UserUpdateProfileDto;
import com.tech.listify.exception.ResourceNotFoundException;
import com.tech.listify.mapper.UserMapper;
import com.tech.listify.model.City;
import com.tech.listify.model.User;
import com.tech.listify.model.enums.AdvertisementStatus;
import com.tech.listify.repository.AdvertisementRepository;
import com.tech.listify.repository.UserRepository;
import com.tech.listify.service.CityService;
import com.tech.listify.service.FileStorageService;
import com.tech.listify.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CityService cityService;
    private final AdvertisementRepository advertisementRepository;
    private final FileStorageService fileStorageService;
    private final UserMapper userMapper;


    @Override
    @Transactional(readOnly = true)
    public UserProfileDto getUserProfileById(Long userId) {
        log.debug("Fetching user profile for ID: {}", userId);
        User user = userRepository.findById(userId).orElseThrow(() ->  new ResourceNotFoundException("Пользователь с ID " + userId + " не найден."));
        int activeAdsCount = advertisementRepository.countBySellerIdAndStatus(user.getId(), AdvertisementStatus.ACTIVE);
        return userMapper.toUserProfileDto(user, activeAdsCount);
    }

    @Override
    public UserProfileDto updateUserProfile(String userEmail, UserUpdateProfileDto updateDto, MultipartFile avatarFile) throws IOException {
        log.info("Attempting to update profile for user: {}", userEmail);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с email " + userEmail + " не найден."));

        boolean profileDataUpdated = false;

        if(updateDto != null) {
            if (updateDto.getFullName() != null && !updateDto.getFullName().isBlank()) {
                user.setFullName(updateDto.getFullName());
                profileDataUpdated = true;
            }
            if (updateDto.getPhoneNumber() != null) {
                user.setPhoneNumber(updateDto.getPhoneNumber().isBlank() ? null : updateDto.getPhoneNumber());
                profileDataUpdated = true;
            }
            if (updateDto.getCityId() != null) {
                if (updateDto.getCityId() <= 0) {
                    user.setCity(null);
                } else {
                    City city = cityService.findCityById(updateDto.getCityId());
                    user.setCity(city);
                }
                profileDataUpdated = true;
            }
        }


        if (avatarFile != null && !avatarFile.isEmpty()) {
            log.debug("Processing new avatar for user: {}", userEmail);
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isBlank()) {
                try {
                    fileStorageService.deleteFile(user.getAvatarUrl());
                    log.debug("Old avatar file deleted: {}", user.getAvatarUrl());
                } catch (IOException e) {
                    log.warn("Could not delete old avatar file {}: {}", user.getAvatarUrl(), e.getMessage());
                }
            }
            String newAvatarUrl = fileStorageService.saveFile(avatarFile, "avatar");
            user.setAvatarUrl(newAvatarUrl);
            log.info("New avatar uploaded for user {}. URL: {}", userEmail, newAvatarUrl);
            profileDataUpdated = true;
        }

        if (profileDataUpdated) {
            userRepository.save(user);
            log.info("Profile updated for user: {}", userEmail);
        } else {
            log.info("No profile changes detected for user: {}", userEmail);
        }

        int activeAdsCount = advertisementRepository.countBySellerIdAndStatus(user.getId(), AdvertisementStatus.ACTIVE);
        return userMapper.toUserProfileDto(user, activeAdsCount);
    }

    @Override
    public UserProfileDto getCurrentUserProfile(String userEmail) {
        log.debug("Fetching current user profile for email: {}", userEmail);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.error("Authenticated user with email {} not found in database.", userEmail);
                    return new ResourceNotFoundException("Пользователь с email " + userEmail + " не найден.");
                });
        int activeAdsCount = advertisementRepository.countBySellerIdAndStatus(user.getId(), AdvertisementStatus.ACTIVE);
        return userMapper.toUserProfileDto(user, activeAdsCount);
    }

}
