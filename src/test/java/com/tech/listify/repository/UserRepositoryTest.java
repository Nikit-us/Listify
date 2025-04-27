package com.tech.listify.repository;

import com.tech.listify.model.Role;
import com.tech.listify.model.User;
import com.tech.listify.model.enums.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Role userRole;

    @BeforeEach
    void setUp() {
        Optional<Role> existingRole = roleRepository.findByName(RoleType.USER);
        if (existingRole.isEmpty()) {
            userRole = new Role();
            userRole.setName(RoleType.USER);
            userRole = entityManager.persistAndFlush(userRole);
        } else {
            userRole = existingRole.get();
        }
    }

    @Test
    @DisplayName("Should save user with role and find by id")
    void shoulSaveAndFindUserById() {
        User newUser = new User();
        newUser.setEmail("test@example.com");
        newUser.setPasswordHash("hashedpassword");
        newUser.setFullName("Test User");
        newUser.setIsActive(true);
        newUser.getRoles().add(userRole);

        User savedUser = userRepository.save(newUser);
        Optional<User> foundUserOpt = userRepository.findById(savedUser.getId());

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull().isPositive();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");

        assertThat(foundUserOpt).isPresent();
        User foundUser = foundUserOpt.get();
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.getFullName()).isEqualTo("Test User");
        assertThat(foundUser.getRoles()).isNotNull().hasSize(1);
        Role actualRole = foundUser.getRoles().iterator().next();
        assertThat(actualRole.getName()).isEqualTo(RoleType.USER);
        // assertThat(foundUser.getCity().getName()).isEqualTo("Testville"); // Если проверяем город
        assertThat(foundUser.getRegistredAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find user by email")
    void shouldFindByEmail() {
        // --- Arrange ---
        User user = new User();
        user.setEmail("findme@example.com");
        user.setPasswordHash("password");
        user.setFullName("Find Me");
        entityManager.persistAndFlush(user);

        Optional<User> foundUserOpt = userRepository.findByEmail("findme@example.com");

        assertThat(foundUserOpt).isPresent();
        assertThat(foundUserOpt.get().getFullName()).isEqualTo("Find Me");
    }

    @Test
    @DisplayName("Should return empty optional when email does not exist")
    void shouldNotFindByNonExistentEmail() {
        Optional<User> foundUserOpt = userRepository.findByEmail("nonexistent@example.com");

        assertThat(foundUserOpt).isNotPresent();
    }

    @Test
    @DisplayName("Should return true when user exists by email")
    void shouldExistByEmail() {
        User user = new User();
        user.setEmail("exists@example.com");
        user.setPasswordHash("password");
        user.setFullName("Exists");
        entityManager.persistAndFlush(user);

        boolean exists = userRepository.existsByEmail("exists@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when user does not exist by email")
    void shouldNotExistByEmail() {
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should correctly map ManyToMany roles relationship")
    void shouldMapRolesCorrectly() {
        Role adminRole = new Role();
        adminRole.setName(RoleType.ADMIN);
        adminRole = entityManager.persistFlushFind(adminRole);

        User user = new User();
        user.setEmail("admin_user@example.com");
        user.setPasswordHash("pwd");
        user.setFullName("Admin User");
        user.getRoles().add(userRole);
        user.getRoles().add(adminRole);

        User savedUser = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        User foundUser = userRepository.findById(savedUser.getId()).orElseThrow();

        assertThat(foundUser.getRoles()).hasSize(2)
                .extracting(Role::getName)
                .containsExactlyInAnyOrder(RoleType.USER, RoleType.ADMIN);
    }
}
