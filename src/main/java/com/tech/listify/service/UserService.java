package com.tech.listify.service;

import com.tech.listify.dto.UserRegistrationDto;
import com.tech.listify.model.User;

public interface UserService {
    /**
     * Регистрирует нового пользователя в системе.
     *
     * @param registrationDto Данные для регистрации.
     * @return Созданная сущность User.
     * @throws UserAlreadyExistsException Если пользователь с таким email уже существует.
     */
    User registerNewUser(UserRegistrationDto registrationDto);
}
