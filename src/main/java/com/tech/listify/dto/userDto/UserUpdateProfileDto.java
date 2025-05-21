package com.tech.listify.dto.userDto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateProfileDto {
    @Size(min = 2, max = 100, message = "Имя должно содержать от 2 до 100 символов")
    private String fullName;

    @Size(max = 50, message = "Номер телефона не может быть длиннее 50 символов")
    private String phoneNumber;

    private Integer cityId;
}
