package com.tech.listify.service;

import com.tech.listify.dto.PageResponseDto;
import com.tech.listify.dto.advertisementdto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdvertisementService {
    AdvertisementDetailDto createAdvertisement(AdvertisementCreateDto createDto, List<MultipartFile> images, String userEmail);

    AdvertisementDetailDto getAdvertisementById(Long id);

    PageResponseDto<AdvertisementResponseDto> getAllActiveAdvertisements(Pageable pageable);

    AdvertisementDetailDto updateAdvertisement(Long id, AdvertisementUpdateDto updateDto, List<MultipartFile> images, String userEmail);

    PageResponseDto<AdvertisementResponseDto> searchAdvertisements(AdvertisementSearchCriteriaDto criteria, Pageable pageable);

    void deleteAdvertisement(Long id, String userEmail);
}
