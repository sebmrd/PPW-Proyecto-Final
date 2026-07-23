package ec.ups.edu.proyectofinal.events.service;

import ec.ups.edu.proyectofinal.events.dto.CategoryRequest;
import ec.ups.edu.proyectofinal.events.dto.CategoryResponse;
import ec.ups.edu.proyectofinal.events.entity.Category;
import ec.ups.edu.proyectofinal.events.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public Page<CategoryResponse> listCategories(Boolean active, Pageable pageable) {
        Page<Category> categories = active == null
                ? categoryRepository.findAll(pageable)
                : categoryRepository.findByActive(active, pageable);
        return categories.map(CategoryResponse::from);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategory(Long id) {
        return categoryRepository.findById(id)
                .map(CategoryResponse::from)
                .orElseThrow(() -> new RuntimeException("Categoria no encontrada"));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new RuntimeException("La categoria ya existe");
        }

        Instant now = Instant.now();
        Category category = new Category();
        category.setName(request.name().trim());
        category.setDescription(blankToNull(request.description()));
        category.setActive(request.active() == null || request.active());
        category.setCreatedAt(now);
        category.setUpdatedAt(now);

        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria no encontrada"));

        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(request.name().trim(), id)) {
            throw new RuntimeException("La categoria ya existe");
        }

        category.setName(request.name().trim());
        category.setDescription(blankToNull(request.description()));
        if (request.active() != null) {
            category.setActive(request.active());
        }
        category.setUpdatedAt(Instant.now());

        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deactivateCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria no encontrada"));
        category.setActive(false);
        category.setUpdatedAt(Instant.now());
        categoryRepository.save(category);
    }

    private String blankToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
