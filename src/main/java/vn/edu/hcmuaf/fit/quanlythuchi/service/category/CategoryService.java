package vn.edu.hcmuaf.fit.quanlythuchi.service.category;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.CategoryDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.CategoryType;

import java.util.List;

@Service
public interface CategoryService {
    /** Lấy toàn bộ hạng mục dạng cây (dùng cho dropdown/tree view) */
    List<CategoryDTO> getCategoryTree();

    /**
     * Lấy danh sách hạng mục phẳng có phân trang và tìm kiếm.
     * @param keyword  Tìm theo name, description
     * @param type     Lọc theo loại: INCOME hoặc EXPENSE
     * @param parentId Lọc theo hạng mục cha
     * @param page     Số trang (1-based)
     * @param size     Số phần tử mỗi trang
     * @param sortBy   Field sắp xếp (mặc định "name")
     * @param sortDir  Chiều sắp xếp: "asc" hoặc "desc"
     */
    Page<CategoryDTO> getAllCategories(String keyword, CategoryType type, Long parentId,
                                      int page, int size, String sortBy, String sortDir);

    CategoryDTO createCategory(CategoryDTO dto);
    CategoryDTO updateCategory(Long id, CategoryDTO dto);
    void deleteCategory(Long id);
    // Thêm hàm lấy chi tiết một hạng mục theo ID
    CategoryDTO getCategoryById(Long id);
}
