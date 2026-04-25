package vn.edu.hcmuaf.fit.quanlythuchi.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Category;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentIsNull();
    List<Category> findByParentIsNullAndIsDeletedFalse();
    Optional<Category> findByIdAndIsDeletedFalse(Long id);
}