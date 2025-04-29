package com.tech.listify.mapper.impl;

import com.tech.listify.dto.*;
import com.tech.listify.mapper.AdvetisementMapper;
import com.tech.listify.model.Advertisement;
import com.tech.listify.model.AdvertisementImage;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class AdvertisementMapperImpl implements AdvetisementMapper {
    @Override
    public Advertisement toAdvertisement(AdvertisementCreateDto dto) {
        if (dto == null) {
            return null;
        }
        Advertisement advertisement = new Advertisement();
        advertisement.setTitle(dto.getTitle());
        advertisement.setDescription(dto.getDescription());
        advertisement.setPrice(dto.getPrice());
        advertisement.setCondition(dto.getCondition());
        return advertisement;
    }

    @Override
    public void updateAdvertisementFromDto(AdvertisementUpdateDto dto, Advertisement ad) {
        if (dto == null || ad == null) {
            return;
        }
        if (dto.getTitle() != null) {
            ad.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            ad.setDescription(dto.getDescription());
        }
        if (dto.getPrice() != null) {
            ad.setPrice(dto.getPrice());
        }
        if (dto.getCondition() != null) {
            ad.setCondition(dto.getCondition());
        }
        if (dto.getStatus() != null) {
            ad.setStatus(dto.getStatus());
        }
    }

    @Override
    public AdvertisementResponseDto toAdvertisementResponseDto(Advertisement ad) {
        if (ad == null) {
            return null;
        }
        AdvertisementResponseDto dto = new AdvertisementResponseDto();
        dto.setId(ad.getId());
        dto.setTitle(ad.getTitle());
        dto.setPrice(ad.getPrice());
        dto.setCreatedAt(ad.getCreatedAt());

        if (ad.getCity() != null) {
            dto.setCityName(ad.getCity().getName());
        }

        Optional<AdvertisementImage> previewImage = ad.getImages().stream()
                .filter(AdvertisementImage::isPreview)
                .findFirst();

        previewImage.ifPresent(image -> dto.setPreviewImageUrl(image.getImageUrl()));

        if (dto.getPreviewImageUrl() == null && !ad.getImages().isEmpty()) {
            dto.setPreviewImageUrl(ad.getImages().getFirst().getImageUrl());
        }
        return dto;

    }

    @Override
    public Page<AdvertisementResponseDto> toAdvertisementResponseDtoPage(Page<Advertisement> advertisementPage) {
        if (advertisementPage == null) {
            return Page.empty();
        }

        return advertisementPage.map(this::toAdvertisementResponseDto);
    }

    @Override
    public AdvertisementDetailDto toAdvertisementDetailDto(Advertisement ad) {
        if (ad == null) {
            return null;
        }
        AdvertisementDetailDto dto = new AdvertisementDetailDto();
        dto.setId(ad.getId());
        dto.setTitle(ad.getTitle());
        dto.setDescription(ad.getDescription());
        dto.setPrice(ad.getPrice());
        dto.setCreatedAt(ad.getCreatedAt());
        dto.setUpdatedAt(ad.getUpdatedAt());
        dto.setStatus(ad.getStatus());
        dto.setCondition(ad.getCondition());

        if (ad.getCategory() != null) {
            dto.setCategoryId(ad.getCategory().getId());
            dto.setCategoryName(ad.getCategory().getName());
        }
        if (ad.getCity() != null) {
            dto.setCityId(ad.getCity().getId());
            dto.setCityName(ad.getCity().getName());
        }
        if (ad.getSeller() != null) {
            dto.setSellerId(ad.getSeller().getId());
            dto.setSellerName(ad.getSeller().getFullName());
        }

        dto.setImages(toImageDtoList(ad.getImages()));

        return dto;
    }

    @Override
    public AdvertisementImageDto toImageDto(AdvertisementImage image) {
        if (image == null) {
            return null;
        }
        AdvertisementImageDto dto = new AdvertisementImageDto();
        dto.setId(image.getId());
        dto.setImageUrl(image.getImageUrl());
        dto.setPreview(image.isPreview());
        return dto;
    }

    @Override
    public List<AdvertisementImageDto> toImageDtoList(List<AdvertisementImage> images) {
        if (images == null || images.isEmpty()) {
            return Collections.emptyList();
        }
        return images.stream()
                .map(this::toImageDto)
                .toList();
    }
}