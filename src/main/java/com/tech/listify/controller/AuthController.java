package com.tech.listify.controller;

import com.tech.listify.dto.UserRegistrationDto;
import com.tech.listify.dto.UserResponseDto;
import com.tech.listify.mapper.UserMapper;
import com.tech.listify.model.User;
import com.tech.listify.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRegistrationDto registrationDto) {
        log.info("Received registration request for email: {}", registrationDto.getEmail());
        User regiserUser = userService.registerNewUser(registrationDto);
        UserResponseDto responseDto = userMapper.toUserResponseDto(regiserUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
}
