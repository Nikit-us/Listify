package com.tech.listify.mapper;

import com.tech.listify.dto.userDto.UserProfileDto;
import com.tech.listify.dto.userDto.UserRegistrationDto;
import com.tech.listify.dto.userDto.UserResponseDto;
import com.tech.listify.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "city", ignore = true)
    @Mapping(target = "registeredAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "advertisements", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    User toUser(UserRegistrationDto dto);

    UserResponseDto toUserResponseDto(User user);

    @Mapping(source = "user.city.id", target = "cityId")
    UserProfileDto toUserProfileDto(User user, Integer totalActiveAds);
}