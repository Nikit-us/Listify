package com.tech.listify.service.impl;

import com.tech.listify.dto.advertisementDto.AdvertisementCreateDto;
import com.tech.listify.dto.advertisementDto.AdvertisementDetailDto;
import com.tech.listify.dto.advertisementDto.AdvertisementResponseDto;
import com.tech.listify.dto.advertisementDto.AdvertisementUpdateDto;
import com.tech.listify.exception.ResourceNotFoundException;
import com.tech.listify.mapper.impl.AdvertisementMapperImpl;
import com.tech.listify.model.*;
import com.tech.listify.model.enums.AdvertisementStatus;
import com.tech.listify.repository.*;
import com.tech.listify.service.AdvertisementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.tech.listify.exception.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdvertisementServiceImpl implements AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CityRepository cityRepository;
    private final AdvertisementMapperImpl advertisementMapper;
    private final AdvertisementImageRepository advertisementImageRepository;

    @Override
    @Transactional
    public AdvertisementDetailDto createAdvertisement(AdvertisementCreateDto createDto, String userEmail) {
        log.info("Creating new advertisement '{}' for user {}", createDto.getTitle(), userEmail);
        User seller = userRepository.findByEmail(userEmail).orElseThrow(() -> {
            log.error("Attempt to create advertisement for non-existent user: {}", userEmail);
            return new ResourceNotFoundException("Пользователь с email '" + userEmail + "' не найден.");
        });

        Category category = categoryRepository.findById(createDto.getCategoryId()).orElseThrow(() -> new ResourceNotFoundException("Категория с ID " + createDto.getCategoryId() + " не найдена."));

        City city = cityRepository.findById(createDto.getCityId()).orElseThrow(() -> new ResourceNotFoundException("Город с ID " + createDto.getCityId() + " не найден."));

        Advertisement newAd = advertisementMapper.toAdvertisement(createDto);

        newAd.setSeller(seller);
        newAd.setCategory(category);
        newAd.setCity(city);
        newAd.setStatus(AdvertisementStatus.ACTIVE);
        log.debug("Advertisement entity populated with seller, category, city, status.");

        if(createDto.getImageUrls() != null && !createDto.getImageUrls().isEmpty()) {
            boolean isFirstImage = true;
            for(String imageUrl : createDto.getImageUrls()) {
                AdvertisementImage image = new AdvertisementImage();
                image.setImageUrl(imageUrl);
                image.setPreview(isFirstImage);
                newAd.addImage(image);
                isFirstImage = false;
                log.trace("Added image with URL: {}", imageUrl);
            }
        } else {
            log.debug("No image URLs provided for the advertisement.");
        }

        Advertisement savedAd = advertisementRepository.save(newAd);
        log.info("Successfully saved new advertisement with ID: {}", savedAd.getId());

        return advertisementMapper.toAdvertisementDetailDto(savedAd);
    }

    @Override
    @Transactional(readOnly = true)
    public AdvertisementDetailDto getAdvertisementById(Long id) {
        log.debug("Fetching advertisement with ID: {}", id);
        Advertisement ad = advertisementRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Объявление с id " + id + " не найдено."));
        return advertisementMapper.toAdvertisementDetailDto(ad);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdvertisementResponseDto> getAllActiveAdvertisements(Pageable pageable) {
        log.debug("Fetching all active advertisements, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Advertisement> activeAdsPage = advertisementRepository.findByStatus(AdvertisementStatus.ACTIVE, pageable);
        log.debug("Found {} active advertisements on page {}", activeAdsPage.getNumberOfElements(), pageable.getPageNumber());
        return advertisementMapper.toAdvertisementResponseDtoPage(activeAdsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public AdvertisementDetailDto updateAdvertisement(Long id, AdvertisementUpdateDto updateDto, String userEmail) {
        log.info("Attempting to update advertisement with ID: {} by user: {}", id, userEmail);

        Advertisement ad = advertisementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Объявление с id " + id + " не найдено."));
        if (!ad.getSeller().getEmail().equals(userEmail)) {
            log.warn("Access denied for user {} to update advertisement ID {}", userEmail, id);
            throw new AccessDeniedException("Вы не можете редактировать это объявление."); // реализовать своё исключение
        }

        if (updateDto.getTitle() != null) {
            ad.setTitle(updateDto.getTitle());
        }
        if (updateDto.getDescription() != null) {
            ad.setDescription(updateDto.getDescription());
        }
        if (updateDto.getPrice() != null) {
            ad.setPrice(updateDto.getPrice());
        }
        if (updateDto.getCondition() != null) {
            ad.setCondition(updateDto.getCondition());
        }

        if(updateDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(updateDto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Категория с ID " + updateDto.getCategoryId() + " не найдена."));
            ad.setCategory(category);
        }

        if(updateDto.getCityId() != null) {
            City city = cityRepository.findById(updateDto.getCityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Город с ID " + updateDto.getCityId() + " не найден."));
            ad.setCity(city);
        }

        if(updateDto.getStatus() != null) {
            ad.setStatus(updateDto.getStatus());
        }

        if(updateDto.getImageUrls() != null) {
            log.debug("Updating images for advertisement ID: {}", id);
            ad.getImages().clear();
            for(String imageUrl: updateDto.getImageUrls()) {
                AdvertisementImage newImage = new AdvertisementImage();
                newImage.setImageUrl(imageUrl.trim());
                if(ad.getImages().isEmpty()){
                    newImage.setPreview(true);
                }
                ad.addImage(newImage);
            }
        }

        Advertisement savedAd = advertisementRepository.save(ad);
        log.info("Successfully updated advertisement with ID: {}", savedAd.getId());
        return advertisementMapper.toAdvertisementDetailDto(savedAd);
    }
}
