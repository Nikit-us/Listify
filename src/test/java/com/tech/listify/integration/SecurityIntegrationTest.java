package com.tech.listify.integration;

import com.tech.listify.model.Role;
import com.tech.listify.model.User;
import com.tech.listify.model.enums.RoleType;
import com.tech.listify.repository.RoleRepository;
import com.tech.listify.repository.UserRepository;
import com.tech.listify.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser; // Для анонимных запросов
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String testUserToken;
    private String testAdminToken;
    private User testUser;
    private User testAdmin;

    @BeforeEach
    void setUp() {
        Role userRole = roleRepository.findByName(RoleType.ROLE_USER).orElseGet(() -> roleRepository.save(new Role(null, RoleType.ROLE_USER, null)));
        Role adminRole = roleRepository.findByName(RoleType.ROLE_ADMIN).orElseGet(() -> roleRepository.save(new Role(null, RoleType.ROLE_ADMIN, null)));

        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("testuser@example.com");
        testUser.setPasswordHash(passwordEncoder.encode("password"));
        testUser.setFullName("Test User");
        testUser.setIsActive(true);
        testUser.setRoles(Set.of(userRole));
        testUser = userRepository.saveAndFlush(testUser);

        testAdmin = new User();
        testAdmin.setEmail("testadmin@example.com");
        testAdmin.setPasswordHash(passwordEncoder.encode("password"));
        testAdmin.setFullName("Test Admin");
        testAdmin.setIsActive(true);
        testAdmin.setRoles(Set.of(userRole, adminRole));
        testAdmin = userRepository.saveAndFlush(testAdmin);

        testUserToken =generateTokenForUser(testUser.getEmail());
        testAdminToken = generateTokenForUser(testAdmin.getEmail());

    }

    @Test
    @DisplayName("GET /api/test/public - Should allow access without authentication")
    @WithAnonymousUser // Явно указываем, что запрос анонимный
    void shouldAllowPublicAccess() throws Exception {
        mockMvc.perform(get("/api/test/public"))
                .andExpect(status().isOk()) // Ожидаем 200 OK
                .andExpect(content().string("This is PUBLIC data."));
    }

    @Test
    @DisplayName("GET /api/test/private - Should return 401 Unauthorized without token")
    @WithAnonymousUser
    void shouldDenyPrivateAccessWithoutToken() throws Exception {
        mockMvc.perform(get("/api/test/private"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/test/private - Should return 401 Unauthorized with invalid token")
    @WithAnonymousUser
    void shouldDenyPrivateAccessWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/test/private")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/test/private - Should allow access with valid user token")
    void shouldAllowPrivateAccessWithValidUserToken() throws Exception {
        mockMvc.perform(get("/api/test/private")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk()) // Ожидаем 200 OK
                .andExpect(content().string("This is PRIVATE data for user: testuser@example.com"));
    }

    @Test
    @DisplayName("GET /api/test/admin - Should return 401 Unauthorized without token")
    @WithAnonymousUser
    void shouldDenyAdminAccessWithoutToken() throws Exception {
        mockMvc.perform(get("/api/test/admin"))
                .andExpect(status().isUnauthorized()); // Ожидаем 401
    }

    // --- Тесты для эндпоинта /api/test/admin (требуют @EnableMethodSecurity) ---

    /* Необходимо добавить @EnableMethodSecurity в SecurityConfig
    @Test
    @DisplayName("GET /api/test/admin - Should return 403 Forbidden with valid user token (USER role)")
    void shouldDenyAdminAccessWithValidUserToken() throws Exception {
        mockMvc.perform(get("/api/test/admin")
                        .header("Authorization", "Bearer " + testUserToken)) // Токен обычного юзера
                .andExpect(status().isForbidden()); // Ожидаем 403 Forbidden
    }

    @Test
    @DisplayName("GET /api/test/admin - Should allow access with valid admin token (ADMIN role)")
    void shouldAllowAdminAccessWithValidAdminToken() throws Exception {
        mockMvc.perform(get("/api/test/admin")
                        .header("Authorization", "Bearer " + testAdminToken)) // Токен админа
                .andExpect(status().isOk()) // Ожидаем 200 OK
                 .andExpect(content().string("This is ADMIN data for user: testadmin@example.com"));
    }
    */

    private String generateTokenForUser(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        return jwtTokenProvider.generateToken(authentication);
    }
}
