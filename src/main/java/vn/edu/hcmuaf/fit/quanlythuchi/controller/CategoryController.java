package vn.edu.hcmuaf.fit.quanlythuchi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.quanlythuchi.config.ApiResponse;
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
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getCategoryTree() {
        return ApiResponse.ok(categoryService.getCategoryTree());
    }

    /**
     * API: Thêm mới một hạng mục
     * Method: POST
     * URL: http://localhost:8080/categories
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryDTO>> createCategory(@RequestBody CategoryDTO categoryDTO) {
        return ApiResponse.created(categoryService.createCategory(categoryDTO), "Tạo hạng mục thành công");
    }

    /**
     * API: Cập nhật thông tin một hạng mục đã có
     * Method: PUT
     * URL: http://localhost:8080/categories/{id}
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryDTO categoryDTO) {
        return ApiResponse.ok(categoryService.updateCategory(id, categoryDTO), "Cập nhật hạng mục thành công");
    }

    /**
     * API: Xóa mềm một hạng mục (Chuyển trạng thái sang INACTIVE)
     * Method: DELETE
     * URL: http://localhost:8080/categories/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.ok(null, "Xóa hạng mục thành công");
    }
}