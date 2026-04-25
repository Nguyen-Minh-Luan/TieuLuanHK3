package vn.edu.hcmuaf.fit.quanlythuchi.service.category;

import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.CategoryDTO;

import java.util.List;

@Service
public interface CategoryService {
    public List<CategoryDTO> getCategoryTree();
    public CategoryDTO createCategory(CategoryDTO dto);
    public CategoryDTO updateCategory(Long id, CategoryDTO dto);
    public void deleteCategory(Long id);
}
