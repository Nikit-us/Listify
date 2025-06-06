package com.tech.listify.mapper;

import com.tech.listify.dto.advertisementDto.*;
import com.tech.listify.model.Advertisement;
import com.tech.listify.model.AdvertisementImage;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

@Mapper(componentModel = "spring")
public interface AdvertisementMapper { // Убедитесь, что имя файла и класса AdvertisementMapper.java

    // ✨ Игнорируем поля, которые устанавливаются в сервисе
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "seller", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "city", ignore = true)
    @Mapping(target = "images", ignore = true)
    Advertisement toAdvertisement(AdvertisementCreateDto dto);

    // ✨ Игнорируем системные поля, которые не должны меняться через этот DTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "seller", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "city", ignore = true)
    @Mapping(target = "images", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateAdvertisementFromDto(AdvertisementUpdateDto dto, @MappingTarget Advertisement ad);

    // ✨ НАШ ИСПРАВЛЕННЫЙ МЕТОД
    @Mapping(source = "city.id", target = "cityId")
    @Mapping(source = "city.name", target = "cityName")
    // Говорим MapStruct: для поля 'previewImageUrl' вызови наш хелпер-метод 'mapPreviewImageUrl'
    @Mapping(target = "previewImageUrl", expression = "java(mapPreviewImageUrl(ad))")
    AdvertisementResponseDto toAdvertisementResponseDto(Advertisement ad);

    default String mapPreviewImageUrl(Advertisement ad) {
        if (ad.getImages() == null || ad.getImages().isEmpty()) {
            return null;
        }
        return ad.getImages().stream()
                .filter(AdvertisementImage::isPreview)
                .findFirst()
                .map(AdvertisementImage::getImageUrl)
                .orElse(ad.getImages().getFirst().getImageUrl());
    }


    default Page<AdvertisementResponseDto> toAdvertisementResponseDtoPage(Page<Advertisement> advertisementPage) {
        if (advertisementPage == null) {
            return Page.empty();
        }
        return advertisementPage.map(this::toAdvertisementResponseDto);
    }

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "city.id", target = "cityId")
    @Mapping(source = "city.name", target = "cityName")
    @Mapping(source = "seller.id", target = "sellerId")
    @Mapping(source = "seller.fullName", target = "sellerName")
    AdvertisementDetailDto toAdvertisementDetailDto(Advertisement ad);

    AdvertisementImageDto toImageDto(AdvertisementImage image);

    List<AdvertisementImageDto> toImageDtoList(List<AdvertisementImage> images);
}