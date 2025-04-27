package com.tech.listify.dto;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class UserResponseDto {
    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private OffsetDateTime registeredAt;

}
