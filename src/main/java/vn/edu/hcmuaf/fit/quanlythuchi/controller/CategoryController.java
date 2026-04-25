package vn.edu.hcmuaf.fit.quanlythuchi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.CategoryDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.service.category.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * API: Lấy danh sách toàn bộ hạng mục theo dạng cây (Cha - Con)
     * Method: GET
     * URL: http://localhost:8080/categories/tree
     */
    @GetMapping("/tree")
    public ResponseEntity<List<CategoryDTO>> getCategoryTree() {
        List<CategoryDTO> categoryTree = categoryService.getCategoryTree();
        return ResponseEntity.ok(categoryTree); // Trả về HTTP Status 200 (OK)
    }

    /**
     * API: Thêm mới một hạng mục
     * Method: POST
     * URL: http://localhost:8080/categories
     */
    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryDTO categoryDTO) {
        System.out.println("đã vào CategoryController");
        CategoryDTO createdCategory = categoryService.createCategory(categoryDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory); // Trả về HTTP Status 201 (Created)
    }

    /**
     * API: Cập nhật thông tin một hạng mục đã có
     * Method: PUT
     * URL: http://localhost:8080/categories/{id}
     */
    @PatchMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryDTO categoryDTO) {

        CategoryDTO updatedCategory = categoryService.updateCategory(id, categoryDTO);
        return ResponseEntity.ok(updatedCategory); // Trả về HTTP Status 200 (OK)
    }

    /**
     * API: Xóa mềm một hạng mục (Chuyển trạng thái sang INACTIVE)
     * Method: DELETE
     * URL: http://localhost:8080/categories/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build(); // Trả về HTTP Status 204 (No Content) - báo hiệu đã xóa thành công và không cần trả về body
    }
}