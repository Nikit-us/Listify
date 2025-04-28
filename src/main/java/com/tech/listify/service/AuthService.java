package com.tech.listify.service;

import com.tech.listify.dto.JwtResponseDto;
import com.tech.listify.dto.LoginRequestDto;
import com.tech.listify.dto.UserRegistrationDto;
import com.tech.listify.dto.UserResponseDto;

public interface AuthService {
    /**
     * Регистрирует нового пользователя в системе.
     *
     * @param registrationDto Данные для регистрации.
     * @return Созданная сущность User.
     * @throws UserAlreadyExistsException Если пользователь с таким email уже существует.
     */
    UserResponseDto register(UserRegistrationDto registrationDto);

    JwtResponseDto login(LoginRequestDto loginRequestDto);
}
