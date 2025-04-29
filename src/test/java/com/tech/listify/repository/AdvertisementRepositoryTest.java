package com.tech.listify.repository;

import com.tech.listify.model.*;
import com.tech.listify.model.enums.ProductCondition;
import com.tech.listify.model.enums.ProductStatus;
import com.tech.listify.model.enums.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AdvertisementRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AdvertisementRepository advertisementRepository;

    // Нужны будут репозитории для связанных сущностей, чтобы их подготовить
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private RoleRepository roleRepository; // Для создания юзера

    private User testSeller;
    private Category testCategory;
    private City testCity;

    @BeforeEach
    void setUp() {
        Role userRole = roleRepository.findByName(RoleType.ROLE_USER)
                .orElseGet(() -> entityManager.persist(new Role(null, RoleType.ROLE_USER, null)));

        testSeller = new User();
        testSeller.setEmail("seller@example.com");
        testSeller.setPasswordHash("password");
        testSeller.setFullName("Test Seller");
        testSeller.getRoles().add(userRole);
        testSeller = entityManager.persistFlushFind(testSeller); // Сохраняем и получаем с ID

        testCategory = new Category();
        testCategory.setName("Electronics");
        testCategory = entityManager.persistFlushFind(testCategory);

        testCity = new City();
        testCity.setName("Test City");
        testCity = entityManager.persistFlushFind(testCity);
    }

    @Test
    @DisplayName("Should save product with relations and enums, then find by ID")
    void shouldSaveAndFindProduct() {
        // --- Arrange ---
        Advertisement newAdvertisement = new Advertisement();
        newAdvertisement.setTitle("Test Laptop");
        newAdvertisement.setDescription("A great laptop for testing.");
        newAdvertisement.setPrice(new BigDecimal("1200.50"));
        newAdvertisement.setStatus(ProductStatus.ACTIVE);
        newAdvertisement.setCondition(ProductCondition.USED_GOOD);
        newAdvertisement.setSeller(testSeller);
        newAdvertisement.setCategory(testCategory);
        newAdvertisement.setCity(testCity);

        Advertisement savedAdvertisement = advertisementRepository.save(newAdvertisement);
        Optional<Advertisement> foundProductOpt = advertisementRepository.findById(savedAdvertisement.getId());

        assertThat(savedAdvertisement).isNotNull();
        assertThat(savedAdvertisement.getId()).isNotNull().isPositive();
        assertThat(savedAdvertisement.getTitle()).isEqualTo("Test Laptop");
        assertThat(savedAdvertisement.getPrice()).isEqualTo(new BigDecimal("1200.50"));
        assertThat(savedAdvertisement.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(savedAdvertisement.getCondition()).isEqualTo(ProductCondition.USED_GOOD);

        assertThat(foundProductOpt).isPresent();
        Advertisement foundAdvertisement = foundProductOpt.get();
        assertThat(foundAdvertisement.getTitle()).isEqualTo("Test Laptop");
        assertThat(foundAdvertisement.getSeller().getId()).isEqualTo(testSeller.getId());
        assertThat(foundAdvertisement.getCategory().getName()).isEqualTo("Electronics");
        assertThat(foundAdvertisement.getCity().getName()).isEqualTo("Test City");
        assertThat(foundAdvertisement.getCreatedAt()).isNotNull();
        assertThat(foundAdvertisement.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find products by status with pagination")
    void shouldFindByStatusWithPagination() {
        Advertisement p1 = new Advertisement(null, "Active Product 1", "Desc", BigDecimal.TEN, ProductStatus.ACTIVE, ProductCondition.NEW, null, null, testSeller, testCategory, testCity, null);
        Advertisement p2 = new Advertisement(null, "Inactive Product", "Desc", BigDecimal.ONE, ProductStatus.INACTIVE, ProductCondition.NEW, null, null, testSeller, testCategory, testCity, null);
        Advertisement p3 = new Advertisement(null, "Active Product 2", "Desc", BigDecimal.TEN, ProductStatus.ACTIVE, ProductCondition.USED_GOOD, null, null, testSeller, testCategory, testCity, null);
        entityManager.persist(p1);
        entityManager.persist(p2);
        entityManager.persist(p3);
        entityManager.flush();

        PageRequest pageable = PageRequest.of(0, 2);
        Page<Advertisement> activeProductsPage = advertisementRepository.findByStatus(ProductStatus.ACTIVE, pageable);

        assertThat(activeProductsPage).isNotNull();
        assertThat(activeProductsPage.getTotalElements()).isEqualTo(2);
        assertThat(activeProductsPage.getTotalPages()).isEqualTo(1);
        assertThat(activeProductsPage.getContent()).hasSize(2);
        assertThat(activeProductsPage.getContent())
                .extracting(Advertisement::getStatus)
                .containsOnly(ProductStatus.ACTIVE); // Убеждаемся, что все найденные - активные
    }

    @Test
    @DisplayName("Should correctly map OneToMany images relationship")
    void shouldMapImagesCorrectly() {
        // --- Arrange ---
        // Используем NoArgsConstructor и сеттеры
        Advertisement advertisement = new Advertisement(); // <--- ИЗМЕНЕНИЕ: Используем конструктор по умолчанию
        advertisement.setTitle("Product With Images");
        advertisement.setDescription("Desc");
        advertisement.setPrice(BigDecimal.TEN);
        advertisement.setStatus(ProductStatus.ACTIVE);
        advertisement.setCondition(ProductCondition.NEW);
        advertisement.setSeller(testSeller);
        advertisement.setCategory(testCategory);
        advertisement.setCity(testCity);
        // Поле 'images' будет инициализировано как new HashSet<>() благодаря инициализатору в классе Product

        AdvertisementImage img1 = new AdvertisementImage(null, null, "http://example.com/img1.jpg", false, null);
        AdvertisementImage img2 = new AdvertisementImage(null, null, "http://example.com/img2.jpg", true, null); // Превью

        // Теперь этот вызов будет работать, т.к. product.images не null
        advertisement.addImage(img1); // <--- Строка 130 (примерно)
        advertisement.addImage(img2);

        // --- Act ---
        Advertisement savedAdvertisement = advertisementRepository.save(advertisement);
        entityManager.flush();
        entityManager.clear();

        Advertisement foundAdvertisement = advertisementRepository.findById(savedAdvertisement.getId()).orElseThrow();

        // --- Assert ---
        assertThat(foundAdvertisement.getImages()).hasSize(2);
        assertThat(foundAdvertisement.getImages())
                .extracting(AdvertisementImage::getImageUrl)
                .containsExactlyInAnyOrder("http://example.com/img1.jpg", "http://example.com/img2.jpg");
        assertThat(foundAdvertisement.getImages())
                .filteredOn(AdvertisementImage::isPreview)
                .hasSize(1)
                .first()
                .extracting(AdvertisementImage::getImageUrl)
                .isEqualTo("http://example.com/img2.jpg");
    }
}