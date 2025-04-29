package com.tech.listify.mapper;

import com.tech.listify.dto.*;
import com.tech.listify.model.Advertisement;
import com.tech.listify.model.AdvertisementImage;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AdvetisementMapper {
    /**
     * Маппит AdvertisementCreateDto в сущность Advertisement.
     * Не устанавливает продавца, категорию, город, изображения - это делается в сервисе.
     * @param dto DTO создания
     * @return Сущность Advertisement
     */
    Advertisement toAdvertisement(AdvertisementCreateDto dto);

    /**
     * Маппит AdvertisementUpdateDto в существующую сущность Advertisement.
     * Обновляет только те поля, которые присутствуют в DTO.
     * @param dto DTO обновления
     * @param advertisement Существующая сущность для обновления
     */
    void updateAdvertisementFromDto(AdvertisementUpdateDto dto, Advertisement advertisement);

    /**
     * Маппит сущность Advertisement в AdvertisementResponseDto (для списков).
     * @param advertisement Сущность
     * @return DTO для списка
     */
    AdvertisementResponseDto toAdvertisementResponseDto(Advertisement advertisement);

    /**
     * Маппит страницу сущностей Advertisement в страницу AdvertisementResponseDto.
     * @param advertisementPage Страница сущностей
     * @return Страница DTO для списка
     */
    Page<AdvertisementResponseDto> toAdvertisementResponseDtoPage(Page<Advertisement> advertisementPage);

    /**
     * Маппит сущность Advertisement в AdvertisementDetailDto (для детального просмотра).
     * @param advertisement Сущность
     * @return Детальное DTO
     */
    AdvertisementDetailDto toAdvertisementDetailDto(Advertisement advertisement);

    /**
     * Маппит сущность AdvertisementImage в AdvertisementImageDto.
     * @param image Сущность изображения
     * @return DTO изображения
     */
    AdvertisementImageDto toImageDto(AdvertisementImage image);

    /**
     * Маппит список сущностей AdvertisementImage в список AdvertisementImageDto.
     * @param images Список сущностей изображений
     * @return Список DTO изображений
     */
    List<AdvertisementImageDto> toImageDtoList(List<AdvertisementImage> images);
}
