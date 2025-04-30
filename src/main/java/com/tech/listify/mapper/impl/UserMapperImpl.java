package com.tech.listify.mapper.impl;

import com.tech.listify.dto.userDto.UserRegistrationDto;
import com.tech.listify.dto.userDto.UserResponseDto;
import com.tech.listify.mapper.UserMapper;
import com.tech.listify.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapperImpl implements UserMapper {
    @Override
    public User toUser(UserRegistrationDto dto) {
        if(dto == null){
            return null;
        }

        User user = new User();
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setIsActive(true);

        return user;
    }

    @Override
    public UserResponseDto toUserResponseDto(User user) {
        if(user == null){
            return null;
        }

        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setId(user.getId());
        userResponseDto.setEmail(user.getEmail());
        userResponseDto.setFullName(user.getFullName());
        userResponseDto.setPhoneNumber(user.getPhoneNumber());
        userResponseDto.setRegisteredAt(user.getRegisteredAt());
        return userResponseDto;
    }

}
