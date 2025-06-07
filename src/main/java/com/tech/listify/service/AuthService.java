package com.tech.listify.service;

import com.tech.listify.dto.userdto.JwtResponseDto;
import com.tech.listify.dto.userdto.LoginRequestDto;
import com.tech.listify.dto.userdto.UserRegistrationDto;
import com.tech.listify.dto.userdto.UserResponseDto;
import org.springframework.web.multipart.MultipartFile;
import com.tech.listify.exception.UserAlreadyExistsException;

import java.io.IOException;

public interface AuthService {
    /**
     * Регистрирует нового пользователя в системе, возможно с аватаром.
     *
     * @param registrationDto Данные для регистрации.
     * @param avatarFile      Опциональный файл аватара.
     * @return DTO ответа с данными пользователя.
     * @throws UserAlreadyExistsException Если пользователь с таким email уже существует.
     * @throws IOException                Если произошла ошибка при сохранении аватара.
     */
    UserResponseDto register(UserRegistrationDto registrationDto, MultipartFile avatarFile) throws IOException;

    JwtResponseDto login(LoginRequestDto loginRequestDto);
}
