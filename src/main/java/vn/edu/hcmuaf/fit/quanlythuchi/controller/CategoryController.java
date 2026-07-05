package vn.edu.hcmuaf.fit.quanlythuchi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.quanlythuchi.config.ApiResponse;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.CategoryDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.PagedResponseDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.CategoryType;
import vn.edu.hcmuaf.fit.quanlythuchi.service.category.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    /**
     * API: Lấy chi tiết một hạng mục theo ID
     * Method: GET
     * URL: http://localhost:8080/categories/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> getCategoryById(@PathVariable Long id) {
        return ApiResponse.ok(categoryService.getCategoryById(id), "Lấy thông tin hạng mục thành công");
    }
    /**
     * API: Lấy danh sách toàn bộ hạng mục theo dạng cây (Cha - Con)
     * Dùng cho dropdown/tree renderer — KHÔNG phân trang.
     * Method: GET
     * URL: http://localhost:8080/categories/tree
     */
    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getCategoryTree() {
        return ApiResponse.ok(categoryService.getCategoryTree());
    }

    /**
     * API: Lấy danh sách hạng mục phẳng có phân trang và tìm kiếm
     * Method: GET
     * URL: http://localhost:8080/categories
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponseDTO<CategoryDTO>>> getAllCategories(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long parentId,
            @RequestParam(defaultValue = "1")    int page,
            @RequestParam(defaultValue = "10")   int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc")  String sortDir) {
        CategoryType categoryType = (type != null) ? CategoryType.valueOf(type.toUpperCase()) : null;
        return ApiResponse.ok(
                PagedResponseDTO.from(
                        categoryService.getAllCategories(keyword, categoryType, parentId,
                                                        page, size, sortBy, sortDir)));
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
     * Method: PATCH
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
