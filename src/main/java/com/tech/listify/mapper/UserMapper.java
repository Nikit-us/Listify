package com.tech.listify.mapper;

import com.tech.listify.dto.userDto.UserProfileDto;
import com.tech.listify.dto.userDto.UserRegistrationDto;
import com.tech.listify.dto.userDto.UserResponseDto;
import com.tech.listify.model.User;

public interface UserMapper {
    /**
     * Преобразует DTO регистрации в сущность User.
     * Пароль не копируется напрямую, его нужно будет хешировать в сервисе.
     * Роли и город не устанавливаются здесь.
     *
     * @param dto DTO с данными для регистрации.
     * @return Сущность User, готовую к дальнейшей обработке (установка пароля, ролей).
     */
    User toUser(UserRegistrationDto dto);

    /**
     * Преобразует сущность User в DTO для ответа.
     *
     * @param user сущность с бд.
     * @return DTO ответа.
     */
    UserResponseDto toUserResponseDto(User user);

    UserProfileDto toUserProfileDto(User user, Integer totalActiveAds);
}
