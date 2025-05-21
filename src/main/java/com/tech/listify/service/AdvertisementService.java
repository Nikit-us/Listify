package com.tech.listify.service;

import com.tech.listify.dto.advertisementDto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdvertisementService {
    /**
     * Создает новое объявление.
     *
     * @param createDto DTO с данными для создания.
     * @param userEmail Email пользователя, создающего объявление (продавца).
     * @return DTO с детальной информацией о созданном объявлении.
     * @throws ResourceNotFoundException если категория, город или пользователь не найдены.
     */
    AdvertisementDetailDto createAdvertisement(AdvertisementCreateDto createDto, List<MultipartFile> images, String userEmail);

    /**
     * Получает объявление по его ID.
     *
     * @param id ID объявления.
     * @return DTO с детальной информацией об объявлении.
     * @throws ResourceNotFoundException если объявление не найдено.
     */
    AdvertisementDetailDto getAdvertisementById(Long id);

    /**
     * Получает страницу активных объявлений.
     *
     * @param pageable Параметры пагинации.
     * @return Страница с DTO объявлений для списка.
     */
    Page<AdvertisementResponseDto> getAllActiveAdvertisements(Pageable pageable);

    /**
     * Обновляет существующее объявление.
     *
     * @param id ID обновляемого объявления.
     * @param updateDto DTO с данными для обновления.
     * @param userEmail Email пользователя, выполняющего обновление.
     * @return DTO с детальной информацией об обновленном объявлении.
     * @throws ResourceNotFoundException если объявление, категория или город не найдены.
     * @throws AccessDeniedException если пользователь не является владельцем объявления.
     */
    AdvertisementDetailDto updateAdvertisement(Long id, AdvertisementUpdateDto updateDto, List<MultipartFile> images, String userEmail);

    /**
     * Ищет и фильтрует объявления на основе критериев.
     *
     * @param criteria DTO с критериями поиска.
     * @param pageable Параметры пагинации и сортировки.
     * @return Страница с найденными объявлениями.
     */
    Page<AdvertisementResponseDto> searchAdvertisements(AdvertisementSearchCriteriaDto criteria, Pageable pageable);

    void deleteAdvertisement(Long id, String userEmail);
}
