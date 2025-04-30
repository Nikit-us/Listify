package com.tech.listify.service;

import com.tech.listify.dto.advertisementDto.AdvertisementCreateDto;
import com.tech.listify.dto.advertisementDto.AdvertisementDetailDto;

public interface AdvertisementService {
    /**
     * Создает новое объявление.
     *
     * @param createDto DTO с данными для создания.
     * @param userEmail Email пользователя, создающего объявление (продавца).
     * @return DTO с детальной информацией о созданном объявлении.
     * @throws ResourceNotFoundException если категория, город или пользователь не найдены.
     */
    AdvertisementDetailDto createAdvertisement(AdvertisementCreateDto createDto, String userEmail);

    // AdvertisementDetailDto getAdvertisementById(Long id);
    // Page<AdvertisementResponseDto> getAllActiveAdvertisements(Pageable pageable);
    // AdvertisementDetailDto updateAdvertisement(Long id, AdvertisementUpdateDto updateDto, String userEmail);
    // void deleteAdvertisement(Long id, String userEmail);
}
