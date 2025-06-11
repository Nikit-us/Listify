package com.tech.listify.service.impl;

import com.tech.listify.dto.advertisementdto.*;
import com.tech.listify.exception.FileStorageException;
import com.tech.listify.exception.ResourceNotFoundException;
import com.tech.listify.mapper.AdvertisementMapper;
import com.tech.listify.model.*;
import com.tech.listify.model.enums.AdvertisementStatus;
import com.tech.listify.repository.*;
import com.tech.listify.repository.specification.AdvertisementSpecification;
import com.tech.listify.service.AdvertisementService;
import com.tech.listify.service.CategoryService;
import com.tech.listify.service.CityService;
import com.tech.listify.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.tech.listify.exception.FileStorageException.ErrorType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdvertisementServiceImpl implements AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final UserRepository userRepository;
    private final CategoryService categoryService;
    private final CityService cityService;
    private final AdvertisementMapper advertisementMapper;
    private final AdvertisementImageRepository advertisementImageRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public AdvertisementDetailDto createAdvertisement(AdvertisementCreateDto createDto, List<MultipartFile> images, String sellerEmail) {
        log.info("Creating new advertisement '{}' for user {}", createDto.title(), sellerEmail);
        User seller = userRepository.findByEmail(sellerEmail).orElseThrow(() -> {
            log.error("Attempt to create advertisement for non-existent user: {}", sellerEmail);
            return new ResourceNotFoundException("Пользователь с email '" + sellerEmail + "' не найден.");
        });

        Category category = categoryService.findCategoryById(createDto.categoryId());

        City city = cityService.findCityById(createDto.cityId());
        Advertisement newAd = advertisementMapper.toAdvertisement(createDto);

        newAd.setSeller(seller);
        newAd.setCategory(category);
        newAd.setCity(city);
        newAd.setStatus(AdvertisementStatus.ACTIVE);
        log.debug("Advertisement entity populated with seller, category, city, status.");

        Advertisement savedAd = advertisementRepository.save(newAd);
        log.info("Saved initial advertisment with ID {}", savedAd.getId());

        List<AdvertisementImage> savedImageEntities = processAndSaveImages(images, savedAd);
        savedAd.setImages(savedImageEntities);
        log.info("Successfully created advertisement with ID: {} including {} images.", savedAd.getId(), savedImageEntities.size());

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
    @Transactional
    public void deleteAdvertisement(Long id, String userEmail){
        log.info("Attempting to delete advertisement with ID: {} by user: {}", id, userEmail);
        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Объявление с ID " + id + " не найдено."));

        // Проверка прав доступа
        if (!advertisement.getSeller().getEmail().equals(userEmail)) {
            log.warn("Access denied for user {} to delete advertisement ID {}", userEmail, id);
            throw new AccessDeniedException("Вы не можете удалять это объявление.");
        }

        List<AdvertisementImage> imagesToDelete = advertisement.getImages();

        for (AdvertisementImage image : imagesToDelete) {
            try {
                fileStorageService.deleteFile(image.getImageUrl());
            } catch (IOException e) {
                log.error("Failed to delete image {}", image.getImageUrl(), e);
            }
        }
        // Удаляем само объявление
        advertisementRepository.delete(advertisement);
        log.info("Successfully deleted advertisement with ID: {}", id);
    }

    @Override
    @Transactional
    public AdvertisementDetailDto updateAdvertisement(Long id, AdvertisementUpdateDto updateDto, List<MultipartFile> newImageFiles, String userEmail) {
        log.info("Attempting to update advertisement with ID: {} by user: {}", id, userEmail);

        Advertisement ad = advertisementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Объявление с id " + id + " не найдено."));

        checkOwnership(ad, userEmail);

        advertisementMapper.updateAdvertisementFromDto(updateDto, ad);

        if (updateDto.categoryId() != null) {
            Category category = categoryService.findCategoryById(updateDto.categoryId());
            ad.setCategory(category);
        }
        if (updateDto.cityId() != null) {
            City city = cityService.findCityById(updateDto.cityId());
            ad.setCity(city);
        }

        if (newImageFiles != null) {
            log.debug("Replacing images for advertisement ID: {}", id);
            ad.getImages().clear();
            List<AdvertisementImage> savedImageEntities = processAndSaveImages(newImageFiles, ad);
            ad.setImages(savedImageEntities);
        }

        Advertisement savedAd = advertisementRepository.save(ad);
        log.info("Successfully updated advertisement with ID: {}", savedAd.getId());

        return advertisementMapper.toAdvertisementDetailDto(savedAd);
    }

    private void checkOwnership(Advertisement advertisement, String userEmail) {
        if (!advertisement.getSeller().getEmail().equals(userEmail)) {
            log.warn("Access denied for user {} to modify advertisement ID {}", userEmail, advertisement.getId());
            throw new AccessDeniedException("Вы не можете редактировать это объявление.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdvertisementResponseDto> searchAdvertisements(AdvertisementSearchCriteriaDto criteria, Pageable pageable) {
        log.debug("Searching advertisements with criteria: {} and pageable: {}", criteria, pageable);

        Specification<Advertisement> specification = AdvertisementSpecification.fromCriteria(criteria);

        Page<Advertisement> advertisementPage = advertisementRepository.findAll(specification, pageable);
        log.debug("Found {} advertisements matching criteria.", advertisementPage.getTotalElements());

        return advertisementMapper.toAdvertisementResponseDtoPage(advertisementPage);
    }


    private List<AdvertisementImage> processAndSaveImages(List<MultipartFile> imageFiles, Advertisement advertisement) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            return new ArrayList<>();
        }

        List<AdvertisementImage> savedImages = new ArrayList<>();
        boolean firstImage = true;
        for (MultipartFile imageFile : imageFiles) {
            if (imageFile != null && !imageFile.isEmpty()) {
                try {
                    String relativeUrl = fileStorageService.saveFile(imageFile, "ad_image"); // Тип контента
                    AdvertisementImage adImage = new AdvertisementImage();
                    adImage.setImageUrl(relativeUrl);
                    adImage.setAdvertisement(advertisement); // Связь
                    adImage.setPreview(firstImage);
                    firstImage = false;
                    savedImages.add(advertisementImageRepository.save(adImage)); // Сохраняем картинку
                } catch (IOException e) {
                    log.error("Failed to store image {} for ad {}: {}", imageFile.getOriginalFilename(), advertisement.getId(), e.getMessage());
                    // Выбрасываем исключение, чтобы откатить транзакцию
                    throw new FileStorageException("Ошибка при сохранении изображения: " + imageFile.getOriginalFilename(), e, ErrorType.SERVER_ERROR);
                }
            }
        }
        return savedImages;
    }
}