package com.tech.listify.service.impl;

import com.tech.listify.dto.advertisementDto.AdvertisementCreateDto;
import com.tech.listify.dto.advertisementDto.AdvertisementDetailDto;
import com.tech.listify.exception.ResourceNotFoundException;
import com.tech.listify.mapper.impl.AdvertisementMapperImpl;
import com.tech.listify.model.*;
import com.tech.listify.model.enums.AdvertisementStatus;
import com.tech.listify.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvertisementServiceImplTest {

    @Mock
    private AdvertisementRepository advertisementRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CityRepository cityRepository;
    @Mock
    private AdvertisementMapperImpl advertisementMapper;


    @InjectMocks
    private AdvertisementServiceImpl advertisementService;

    private AdvertisementCreateDto createDto;
    private User testUser;
    private Category testCategory;
    private City testCity;
    private Advertisement baseAd;
    private Advertisement savedAd;
    private AdvertisementDetailDto expectedDetailDto;
    private String userEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        // Готовим DTO
        createDto = new AdvertisementCreateDto();
        createDto.setTitle("Test Ad Title");
        createDto.setDescription("Test Description");
        createDto.setPrice(BigDecimal.valueOf(100.00));
        createDto.setCategoryId(1);
        createDto.setCityId(1);
        createDto.setImageUrls(List.of("url1.jpg", "url2.jpg"));

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail(userEmail);
        testUser.setFullName("Test User");

        testCategory = new Category();
        testCategory.setId(1);
        testCategory.setName("Test Category");

        testCity = new City();
        testCity.setId(1);
        testCity.setName("Test City");

        baseAd = new Advertisement();
        baseAd.setTitle(createDto.getTitle());
        baseAd.setDescription(createDto.getDescription());
        baseAd.setPrice(createDto.getPrice());

        savedAd = new Advertisement();
        savedAd.setId(10L);
        savedAd.setTitle(baseAd.getTitle());
        savedAd.setDescription(baseAd.getDescription());
        savedAd.setPrice(baseAd.getPrice());
        savedAd.setSeller(testUser);
        savedAd.setCategory(testCategory);
        savedAd.setCity(testCity);
        savedAd.setStatus(AdvertisementStatus.ACTIVE);
        savedAd.setCreatedAt(OffsetDateTime.now());
        savedAd.setUpdatedAt(OffsetDateTime.now());
        AdvertisementImage img1 = new AdvertisementImage(1L, savedAd, "url1.jpg", true, OffsetDateTime.now());
        AdvertisementImage img2 = new AdvertisementImage(2L, savedAd, "url2.jpg", false, OffsetDateTime.now());
        savedAd.setImages(List.of(img1, img2));

        expectedDetailDto = new AdvertisementDetailDto();
        expectedDetailDto.setId(savedAd.getId());
        expectedDetailDto.setTitle(savedAd.getTitle());
        expectedDetailDto.setPrice(savedAd.getPrice());
    }

    @Test
    @DisplayName("createAdvertisement - Should create and save advertisement successfully")
    void createAdvertisement_Success() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(createDto.getCategoryId())).thenReturn(Optional.of(testCategory));
        when(cityRepository.findById(createDto.getCityId())).thenReturn(Optional.of(testCity));
        when(advertisementMapper.toAdvertisement(createDto)).thenReturn(baseAd); // Маппер возвращает базовый Ad
        when(advertisementRepository.save(any(Advertisement.class))).thenReturn(savedAd); // Репозиторий возвращает Ad с ID
        when(advertisementMapper.toAdvertisementDetailDto(savedAd)).thenReturn(expectedDetailDto); // Маппер DTO

        AdvertisementDetailDto resultDto = advertisementService.createAdvertisement(createDto, null,userEmail);

        assertThat(resultDto).isNotNull().isEqualTo(expectedDetailDto);

        ArgumentCaptor<Advertisement> adCaptor = ArgumentCaptor.forClass(Advertisement.class);
        verify(advertisementRepository).save(adCaptor.capture());
        Advertisement capturedAd = adCaptor.getValue();

        assertThat(capturedAd.getSeller()).isEqualTo(testUser);
        assertThat(capturedAd.getCategory()).isEqualTo(testCategory);
        assertThat(capturedAd.getCity()).isEqualTo(testCity);
        assertThat(capturedAd.getStatus()).isEqualTo(AdvertisementStatus.ACTIVE);

        assertThat(capturedAd.getImages()).hasSize(2);
        assertThat(capturedAd.getImages().getFirst().getImageUrl()).isEqualTo("url1.jpg");
        assertThat(capturedAd.getImages().getFirst().isPreview()).isTrue();
        assertThat(capturedAd.getImages().getFirst().getAdvertisement()).isEqualTo(capturedAd);
        assertThat(capturedAd.getImages().get(1).getImageUrl()).isEqualTo("url2.jpg");
        assertThat(capturedAd.getImages().get(1).isPreview()).isFalse();
        assertThat(capturedAd.getImages().get(1).getAdvertisement()).isEqualTo(capturedAd);

        verify(userRepository).findByEmail(userEmail);
        verify(categoryRepository).findById(createDto.getCategoryId());
        verify(cityRepository).findById(createDto.getCityId());
        verify(advertisementMapper).toAdvertisement(createDto);
        verify(advertisementMapper).toAdvertisementDetailDto(savedAd);
        verifyNoMoreInteractions(userRepository, categoryRepository, cityRepository, advertisementRepository, advertisementMapper);
    }

    @Test
    @DisplayName("createAdvertisement - Should throw ResourceNotFoundException if user not found")
    void createAdvertisement_UserNotFound() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> advertisementService.createAdvertisement(createDto, null, userEmail))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Пользователь с email '" + userEmail + "' не найден.");

        verify(userRepository).findByEmail(userEmail);
        verifyNoInteractions(categoryRepository, cityRepository, advertisementRepository, advertisementMapper);
    }

    @Test
    @DisplayName("createAdvertisement - Should throw ResourceNotFoundException if category not found")
    void createAdvertisement_CategoryNotFound() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(createDto.getCategoryId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> advertisementService.createAdvertisement(createDto, null, userEmail))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Категория с ID " + createDto.getCategoryId() + " не найдена.");

        verify(userRepository).findByEmail(userEmail);
        verify(categoryRepository).findById(createDto.getCategoryId());
        verifyNoInteractions(cityRepository, advertisementRepository, advertisementMapper);
    }

    @Test
    @DisplayName("createAdvertisement - Should throw ResourceNotFoundException if city not found")
    void createAdvertisement_CityNotFound() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(createDto.getCategoryId())).thenReturn(Optional.of(testCategory));
        when(cityRepository.findById(createDto.getCityId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> advertisementService.createAdvertisement(createDto, null, userEmail))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Город с ID " + createDto.getCityId() + " не найден.");

        verify(userRepository).findByEmail(userEmail);
        verify(categoryRepository).findById(createDto.getCategoryId());
        verify(cityRepository).findById(createDto.getCityId());
        verifyNoInteractions(advertisementRepository, advertisementMapper);
    }

    @Test
    @DisplayName("createAdvertisement - Should handle case with no image URLs provided")
    void createAdvertisement_NoImages() {
        createDto.setImageUrls(null);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(createDto.getCategoryId())).thenReturn(Optional.of(testCategory));
        when(cityRepository.findById(createDto.getCityId())).thenReturn(Optional.of(testCity));
        when(advertisementMapper.toAdvertisement(createDto)).thenReturn(baseAd);

        savedAd.setImages(List.of());
        when(advertisementRepository.save(any(Advertisement.class))).thenReturn(savedAd);
        when(advertisementMapper.toAdvertisementDetailDto(savedAd)).thenReturn(expectedDetailDto);

        AdvertisementDetailDto resultDto = advertisementService.createAdvertisement(createDto, null, userEmail);

        assertThat(resultDto).isNotNull();

        ArgumentCaptor<Advertisement> adCaptor = ArgumentCaptor.forClass(Advertisement.class);
        verify(advertisementRepository).save(adCaptor.capture());
        Advertisement capturedAd = adCaptor.getValue();

        assertThat(capturedAd.getImages()).isNotNull().isEmpty();
    }
}