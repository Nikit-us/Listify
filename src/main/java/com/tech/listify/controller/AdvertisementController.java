package com.tech.listify.controller;

import com.tech.listify.dto.advertisementDto.AdvertisementCreateDto;
import com.tech.listify.dto.advertisementDto.AdvertisementDetailDto;
import com.tech.listify.dto.advertisementDto.AdvertisementResponseDto;
import com.tech.listify.dto.advertisementDto.AdvertisementUpdateDto;
import com.tech.listify.service.AdvertisementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/ads")
@RequiredArgsConstructor
@Slf4j
public class AdvertisementController {
    private final AdvertisementService advertisementService;

    @PostMapping
    public ResponseEntity<AdvertisementDetailDto> createAdvertisement(@Valid @RequestPart("advertisement") AdvertisementCreateDto createDto,
                                                                      @RequestPart(value = "images", required = false) List<MultipartFile> images,
                                                                      Authentication authentication) {
        String userEmail = authentication.getName();
        log.info("Received request to create advertisement from user: {}", userEmail);
        AdvertisementDetailDto createdAdDto = advertisementService.createAdvertisement(createDto, images, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAdDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdvertisementDetailDto> getAdvertisement(@PathVariable Long id) {
        log.debug("Received request to get advertisement by ID: {}", id);
        AdvertisementDetailDto adDto = advertisementService.getAdvertisementById(id);
        return ResponseEntity.status(HttpStatus.OK).body(adDto);
    }

    @GetMapping
    public ResponseEntity<Page<AdvertisementResponseDto>> getAllAdvertisements(
            // @PageableDefault для настройки сортировки/размера по умолчанию
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.debug("Received request to get all advertisements, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<AdvertisementResponseDto> advertisementPage = advertisementService.getAllActiveAdvertisements(pageable);
        return ResponseEntity.ok(advertisementPage);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdvertisementDetailDto> updateAdvertisement(
            @PathVariable Long id,
            @Valid @RequestPart("advertisement") AdvertisementUpdateDto updateDto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            Authentication authentication) {
        String userEmail = authentication.getName();
        log.info("Received request to update advertisement ID: {} from user: {}", id, userEmail);
        AdvertisementDetailDto adDto = advertisementService.updateAdvertisement(id, updateDto, images, userEmail);
        return ResponseEntity.status(HttpStatus.OK).body(adDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdvertisement(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        log.info("Received request to delete advertisement ID: {} from user: {}", id, userEmail);
        advertisementService.deleteAdvertisement(id, userEmail);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

}
