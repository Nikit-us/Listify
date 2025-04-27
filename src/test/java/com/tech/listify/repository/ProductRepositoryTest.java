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
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

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
        Product newProduct = new Product();
        newProduct.setTitle("Test Laptop");
        newProduct.setDescription("A great laptop for testing.");
        newProduct.setPrice(new BigDecimal("1200.50"));
        newProduct.setStatus(ProductStatus.ACTIVE);
        newProduct.setCondition(ProductCondition.USED_GOOD);
        newProduct.setSeller(testSeller);
        newProduct.setCategory(testCategory);
        newProduct.setCity(testCity);

        Product savedProduct = productRepository.save(newProduct);
        Optional<Product> foundProductOpt = productRepository.findById(savedProduct.getId());

        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getId()).isNotNull().isPositive();
        assertThat(savedProduct.getTitle()).isEqualTo("Test Laptop");
        assertThat(savedProduct.getPrice()).isEqualTo(new BigDecimal("1200.50"));
        assertThat(savedProduct.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(savedProduct.getCondition()).isEqualTo(ProductCondition.USED_GOOD);

        assertThat(foundProductOpt).isPresent();
        Product foundProduct = foundProductOpt.get();
        assertThat(foundProduct.getTitle()).isEqualTo("Test Laptop");
        assertThat(foundProduct.getSeller().getId()).isEqualTo(testSeller.getId());
        assertThat(foundProduct.getCategory().getName()).isEqualTo("Electronics");
        assertThat(foundProduct.getCity().getName()).isEqualTo("Test City");
        assertThat(foundProduct.getCreatedAt()).isNotNull();
        assertThat(foundProduct.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find products by status with pagination")
    void shouldFindByStatusWithPagination() {
        Product p1 = new Product(null, "Active Product 1", "Desc", BigDecimal.TEN, ProductStatus.ACTIVE, ProductCondition.NEW, null, null, testSeller, testCategory, testCity, null);
        Product p2 = new Product(null, "Inactive Product", "Desc", BigDecimal.ONE, ProductStatus.INACTIVE, ProductCondition.NEW, null, null, testSeller, testCategory, testCity, null);
        Product p3 = new Product(null, "Active Product 2", "Desc", BigDecimal.TEN, ProductStatus.ACTIVE, ProductCondition.USED_GOOD, null, null, testSeller, testCategory, testCity, null);
        entityManager.persist(p1);
        entityManager.persist(p2);
        entityManager.persist(p3);
        entityManager.flush();

        PageRequest pageable = PageRequest.of(0, 2);
        Page<Product> activeProductsPage = productRepository.findByStatus(ProductStatus.ACTIVE, pageable);

        assertThat(activeProductsPage).isNotNull();
        assertThat(activeProductsPage.getTotalElements()).isEqualTo(2);
        assertThat(activeProductsPage.getTotalPages()).isEqualTo(1);
        assertThat(activeProductsPage.getContent()).hasSize(2);
        assertThat(activeProductsPage.getContent())
                .extracting(Product::getStatus)
                .containsOnly(ProductStatus.ACTIVE); // Убеждаемся, что все найденные - активные
    }

    @Test
    @DisplayName("Should correctly map OneToMany images relationship")
    void shouldMapImagesCorrectly() {
        // --- Arrange ---
        // Используем NoArgsConstructor и сеттеры
        Product product = new Product(); // <--- ИЗМЕНЕНИЕ: Используем конструктор по умолчанию
        product.setTitle("Product With Images");
        product.setDescription("Desc");
        product.setPrice(BigDecimal.TEN);
        product.setStatus(ProductStatus.ACTIVE);
        product.setCondition(ProductCondition.NEW);
        product.setSeller(testSeller);
        product.setCategory(testCategory);
        product.setCity(testCity);
        // Поле 'images' будет инициализировано как new HashSet<>() благодаря инициализатору в классе Product

        ProductImage img1 = new ProductImage(null, null, "http://example.com/img1.jpg", false, null);
        ProductImage img2 = new ProductImage(null, null, "http://example.com/img2.jpg", true, null); // Превью

        // Теперь этот вызов будет работать, т.к. product.images не null
        product.addImage(img1); // <--- Строка 130 (примерно)
        product.addImage(img2);

        // --- Act ---
        Product savedProduct = productRepository.save(product);
        entityManager.flush();
        entityManager.clear();

        Product foundProduct = productRepository.findById(savedProduct.getId()).orElseThrow();

        // --- Assert ---
        assertThat(foundProduct.getImages()).hasSize(2);
        assertThat(foundProduct.getImages())
                .extracting(ProductImage::getImageUrl)
                .containsExactlyInAnyOrder("http://example.com/img1.jpg", "http://example.com/img2.jpg");
        assertThat(foundProduct.getImages())
                .filteredOn(ProductImage::isPreview)
                .hasSize(1)
                .first()
                .extracting(ProductImage::getImageUrl)
                .isEqualTo("http://example.com/img2.jpg");
    }
}