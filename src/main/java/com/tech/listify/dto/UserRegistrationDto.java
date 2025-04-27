package com.tech.listify.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegistrationDto {
    @NotBlank(message = "Имя не может быть пустым")
    @Size(min = 2, max = 100, message = "Имя должно содержать от 2 до 100 символов")
    private String fullName;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    @Size(max = 255, message = "Email не может быть длиннее 255 символов")
    private String email;

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 8, max = 255, message = "Пароль должен содержать от 8 до 255 символов")
    // Можно добавить @Pattern для сложности пароля, если нужно
    private String password;

    @Size(max = 50, message = "Номер телефона не может быть длиннее 50 символов")
    private String phoneNumber;
}
