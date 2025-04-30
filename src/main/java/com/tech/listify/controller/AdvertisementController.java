package com.tech.listify.controller;

import com.tech.listify.dto.advertisementDto.AdvertisementCreateDto;
import com.tech.listify.dto.advertisementDto.AdvertisementDetailDto;
import com.tech.listify.model.Advertisement;
import com.tech.listify.service.AdvertisementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/ads")
@RequiredArgsConstructor
@Slf4j
public class AdvertisementController {
    private final AdvertisementService advertisementService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdvertisementDetailDto createAdvertisement(@Valid @RequestBody AdvertisementCreateDto createDto,
                                                      Authentication authentication) {
        String userEmail = authentication.getName();
        log.info("Received request to create advertisement from user: {}", userEmail);
        return advertisementService.createAdvertisement(createDto, userEmail);
    }
}
