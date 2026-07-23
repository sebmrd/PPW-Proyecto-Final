package ec.ups.edu.proyectofinal.events.controller;

import ec.ups.edu.proyectofinal.events.dto.CategoryRequest;
import ec.ups.edu.proyectofinal.events.dto.CategoryResponse;
import ec.ups.edu.proyectofinal.events.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public Page<CategoryResponse> listCategories(
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return categoryService.listCategories(active, pageable);
    }

    @GetMapping("/{id}")
    public CategoryResponse getCategory(@PathVariable Long id) {
        return categoryService.getCategory(id);
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request));
    }

    @PutMapping("/{id}")
    public CategoryResponse updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return categoryService.updateCategory(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateCategory(@PathVariable Long id) {
        categoryService.deactivateCategory(id);
        return ResponseEntity.noContent().build();
    }
}
