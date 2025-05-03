package com.tech.listify.service.impl;

import com.tech.listify.dto.advertisementDto.AdvertisementCreateDto;
import com.tech.listify.dto.advertisementDto.AdvertisementDetailDto;
import com.tech.listify.dto.advertisementDto.AdvertisementResponseDto;
import com.tech.listify.dto.advertisementDto.AdvertisementUpdateDto;
import com.tech.listify.exception.FileStorageException;
import com.tech.listify.exception.ResourceNotFoundException;
import com.tech.listify.mapper.impl.AdvertisementMapperImpl;
import com.tech.listify.model.*;
import com.tech.listify.model.enums.AdvertisementStatus;
import com.tech.listify.repository.*;
import com.tech.listify.service.AdvertisementService;
import com.tech.listify.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.tech.listify.exception.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public AdvertisementDetailDto createAdvertisement(AdvertisementCreateDto createDto, List<MultipartFile> images, String userEmail) {
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
    public void deleteAdvertisement(Long id, String userEmail) {
        log.info("Attempting to delete advertisement with ID: {} by user: {}", id, userEmail);
        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Объявление с ID " + id + " не найдено."));

        // Проверка прав доступа
        if (!advertisement.getSeller().getEmail().equals(userEmail)) {
            log.warn("Access denied for user {} to delete advertisement ID {}", userEmail, id);
            throw new com.tech.listify.exception.AccessDeniedException("Вы не можете удалять это объявление.");
        }

        // Удаляем связанные файлы и записи из БД
        deleteExistingImages(advertisement);

        // Удаляем само объявление
        advertisementRepository.delete(advertisement);
        log.info("Successfully deleted advertisement with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public AdvertisementDetailDto updateAdvertisement(Long id, AdvertisementUpdateDto updateDto, List<MultipartFile> newImages, String userEmail) {
        log.info("Attempting to update advertisement with ID: {} by user: {}", id, userEmail);

        Advertisement ad = advertisementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Объявление с id " + id + " не найдено."));
        if (!ad.getSeller().getEmail().equals(userEmail)) {
            log.warn("Access denied for user {} to update advertisement ID {}", userEmail, id);
            throw new AccessDeniedException("Вы не можете редактировать это объявление."); // реализовать своё исключение
        }

        boolean update = false;

        if (updateDto.getTitle() != null) {
            ad.setTitle(updateDto.getTitle());
            update = true;
        }
        if (updateDto.getDescription() != null) {
            ad.setDescription(updateDto.getDescription());
            update = true;
        }
        if (updateDto.getPrice() != null) {
            ad.setPrice(updateDto.getPrice());
            update = true;
        }
        if (updateDto.getCondition() != null) {
            ad.setCondition(updateDto.getCondition());
            update = true;
        }

        if(updateDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(updateDto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Категория с ID " + updateDto.getCategoryId() + " не найдена."));
            ad.setCategory(category);
            update = true;
        }

        if(updateDto.getCityId() != null) {
            City city = cityRepository.findById(updateDto.getCityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Город с ID " + updateDto.getCityId() + " не найден."));
            ad.setCity(city);
            update = true;
        }

        if(updateDto.getStatus() != null) {
            ad.setStatus(updateDto.getStatus());
            update = true;
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
            update = true;
        }

        if(newImages != null) {
            log.debug("Replacing images for advertisement ID: {}", id);
            deleteExistingImages(ad);
            List<AdvertisementImage> savedImageEntities = processAndSaveImages(newImages, ad);
            ad.setImages(savedImageEntities);
            update = true;
        }

        Advertisement finalAd = update ? advertisementRepository.save(ad) : ad;
        if (update) {
            log.info("Successfully updated advertisement with ID: {}", finalAd.getId());
        }
        else {
            log.info("No changes detected for advertisement ID: {}", id);
        }
        return advertisementMapper.toAdvertisementDetailDto(finalAd);
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
                    throw new FileStorageException("Ошибка при сохранении изображения: " + imageFile.getOriginalFilename(), e);
                }
            }
        }
        return savedImages;
    }

    private void deleteExistingImages(Advertisement advertisement) {
        // Копируем список URL перед удалением сущностей, чтобы знать, какие файлы удалять
        List<String> urlsToDelete = advertisement.getImages().stream()
                .map(AdvertisementImage::getImageUrl)
                .toList();

        // Удаляем сущности из БД (orphanRemoval=true должен сработать при очистке коллекции)
        // Или удаляем явно через репозиторий
        advertisementImageRepository.deleteAll(advertisement.getImages()); // Явное удаление
        advertisement.getImages().clear(); // Очищаем коллекцию
        advertisementRepository.flush(); // Принудительно синхронизируем с БД

        // Удаляем файлы из хранилища
        for (String url : urlsToDelete) {
            try {
                fileStorageService.deleteFile(url);
            } catch (IOException e) {
                log.error("Failed to delete image file {}: {}", url, e.getMessage());
                // Решить: проигнорировать? или Выбросить исключение?
            }
        }
    }
}
