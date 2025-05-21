package com.tech.listify.service;

import com.tech.listify.dto.userDto.UserProfileDto;
import com.tech.listify.dto.userDto.UserUpdateProfileDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserService {
    /**
     * Получает публичный профиль пользователя по его ID.
     * @param userId ID пользователя.
     * @return DTO с профилем пользователя.
     * @throws ResourceNotFoundException если пользователь не найден.
     */
    UserProfileDto getUserProfileById(Long userId);

    /**
     * Обновляет профиль текущего аутентифицированного пользователя,
     * включая текстовые данные и опционально аватар.
     * @param userEmail Email аутентифицированного пользователя.
     * @param updateDto DTO с текстовыми данными для обновления.
     * @param avatarFile Опциональный новый файл аватара. Если null, аватар не меняется.
     *                   Если передан файл, старый аватар (если был) удаляется.
     * @return Обновленный DTO профиля пользователя.
     * @throws ResourceNotFoundException если пользователь или город не найдены.
     * @throws IOException если произошла ошибка при работе с файлом аватара.
     */
    UserProfileDto updateUserProfile(String userEmail, UserUpdateProfileDto updateDto, MultipartFile avatarFile) throws IOException;
    UserProfileDto getCurrentUserProfile(String userEmail);
}
