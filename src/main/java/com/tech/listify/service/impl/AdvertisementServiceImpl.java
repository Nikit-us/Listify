package com.tech.listify.service.impl;

import com.tech.listify.dto.advertisementDto.AdvertisementCreateDto;
import com.tech.listify.dto.advertisementDto.AdvertisementDetailDto;
import com.tech.listify.exception.ResourceNotFoundException;
import com.tech.listify.mapper.impl.AdvertisementMapperImpl;
import com.tech.listify.model.*;
import com.tech.listify.model.enums.AdvertisementStatus;
import com.tech.listify.repository.AdvertisementRepository;
import com.tech.listify.repository.CategoryRepository;
import com.tech.listify.repository.CityRepository;
import com.tech.listify.repository.UserRepository;
import com.tech.listify.service.AdvertisementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

        List<AdvertisementImage> images = new ArrayList<>();
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
}
