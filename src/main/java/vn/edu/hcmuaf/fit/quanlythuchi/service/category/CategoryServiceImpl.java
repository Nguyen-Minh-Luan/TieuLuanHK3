package vn.edu.hcmuaf.fit.quanlythuchi.service.category;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.CategoryDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Category;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.CategoryType;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.CategoryRepository;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService{
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getCategoryTree() {
        // CẬP NHẬT: Chỉ lấy các hạng mục Cha gốc chưa bị xóa
        List<Category> rootCategories = categoryRepository.findByParentIsNullAndIsDeletedFalse();
        return rootCategories.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDTO> getAllCategories(String keyword, CategoryType type, Long parentId,
                                             int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, sort);
        return categoryRepository.searchCategories(keyword, type, parentId, pageable)
                                 .map(this::mapToDTO);
    }

    @Override
    @Transactional
    public CategoryDTO createCategory(CategoryDTO dto) {
        Category category = Category.builder()
                .name(dto.getName())
                .type(CategoryType.valueOf(dto.getType().toUpperCase()))
                .description(dto.getDescription())
                .budgeting(dto.getBudgeting())
                .tax(dto.getTax())
                .isDeleted(false) // Mặc định khi tạo là chưa xóa
                .build();

        if (dto.getParentId() != null) {
            Category parent = categoryRepository.findByIdAndIsDeletedFalse(dto.getParentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Hạng mục Cha hoặc Hạng mục đã bị xóa"));
            category.setParent(parent);
        }

        Category savedCategory = categoryRepository.save(category);
        return mapToDTO(savedCategory);
    }

    @Override
    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO dto) {
        Category category = categoryRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Hạng mục"));
        if (dto.getName() != null) {
            category.setName(dto.getName());
        }
        if (dto.getType() != null) {
            category.setType(CategoryType.valueOf(dto.getType().toUpperCase()));
        }

        // Trường description: Nếu không gửi description trong JSON, nó sẽ giữ nguyên giá trị cũ
        if (dto.getDescription() != null) {
            category.setDescription(dto.getDescription());
        }

        if (dto.getBudgeting() != null) {
            category.setBudgeting(dto.getBudgeting());
        }

        if (dto.getTax() != null) {
            category.setTax(dto.getTax());
        }

        // 3. Xử lý logic parentId đặc biệt
        if (dto.getParentId() != null) {
            if (id.equals(dto.getParentId())) {
                throw new RuntimeException("Hạng mục không thể làm cha của chính nó");
            }

            // Tìm cha mới
            Category newParent = categoryRepository.findByIdAndIsDeletedFalse(dto.getParentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Cha mới"));

            category.setParent(newParent);
        }
        // 4. Lưu lại
        Category updatedCategory = categoryRepository.save(category);
        return mapToDTO(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        // 1. Lấy hạng mục cần xóa ra
        Category category = categoryRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Hạng mục hoặc Hạng mục đã bị xóa"));

        // 2. CHECK: Hạng mục này có danh sách con và trong đó có đứa nào đang "sống" (chưa bị xóa) không?
        boolean hasActiveChildren = false;
        if (category.getChildren() != null) {
            for (Category child : category.getChildren()) {
                if (!child.getIsDeleted()) {
                    hasActiveChildren = true;
                    break; // Chỉ cần phát hiện 1 đứa con đang sống là dừng kiểm tra ngay
                }
            }
        }

        // 3. XỬ LÝ THEO LOGIC CỦA BẠN
        if (hasActiveChildren) {
            // CÁCH A (Khuyên dùng): Báo lỗi, bắt người dùng phải tự tay xóa con trước để tránh xóa nhầm dữ liệu quan trọng
            throw new RuntimeException("Không thể xóa! Hạng mục này đang chứa các hạng mục con. Vui lòng xóa hoặc di chuyển các hạng mục con trước.");

            /* CÁCH B: Nếu bạn vẫn muốn hệ thống tự động xóa hết con bằng vòng lặp For, bạn comment lệnh throw ở trên lại và mở comment đoạn này ra:

            for (Category child : category.getChildren()) {
                if (!child.getIsDeleted()) {
                    child.setIsDeleted(true);
                }
            }
            */
        }

        // 4. Nếu là hạng mục con (không có children) HOẶC đã check xong cha -> Tiến hành xóa nó
        category.setIsDeleted(true);
        categoryRepository.save(category);
    }

    private CategoryDTO mapToDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .type(category.getType().name())
                .description(category.getDescription())
                .budgeting(category.getBudgeting())
                .tax(category.getTax())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .build();
    }
}
