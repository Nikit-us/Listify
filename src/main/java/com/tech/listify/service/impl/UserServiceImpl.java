package com.tech.listify.service.impl;

import com.tech.listify.dto.UserRegistrationDto;
import com.tech.listify.exception.UserAlreadyExistsException;
import com.tech.listify.mapper.UserMapper;
import com.tech.listify.model.Role;
import com.tech.listify.model.User;
import com.tech.listify.model.enums.RoleType;
import com.tech.listify.repository.RoleRepository;
import com.tech.listify.repository.UserRepository;
import com.tech.listify.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;


}
