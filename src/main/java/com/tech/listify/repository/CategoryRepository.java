package com.tech.listify.repository;

import com.tech.listify.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Optional<Category> findByNameIgnoreCase(String name);
    List<Category> findByParentCategoryIsNull();
    List<Category> findByParentCategoryId(Integer parentId);
}
