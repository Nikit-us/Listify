package com.tech.listify.service;

import com.tech.listify.dto.userdto.UserProfileDto;
import com.tech.listify.dto.userdto.UserUpdateProfileDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserService {
    UserProfileDto getUserProfileById(Long userId);

    UserProfileDto updateUserProfile(String userEmail, UserUpdateProfileDto updateDto, MultipartFile avatarFile) throws IOException;

    UserProfileDto getCurrentUserProfile(String userEmail);
}
