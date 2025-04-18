package org.nikita.listify.repository;

import org.nikita.listify.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Spring Data JPA автоматически сгенерирует запрос для поиска по username
    Optional<User> findByUsername(String username);

    // Поиск по email
    Optional<User> findByEmail(String email);

    // Проверка существования по username (эффективнее, чем findByUsername().isPresent())
    boolean existsByUsername(String username);

    // Проверка существования по email
    boolean existsByEmail(String email);
}
