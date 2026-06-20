package vn.edu.hcmuaf.fit.quanlythuchi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Category;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.CategoryType;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentIsNull();
    List<Category> findByParentIsNullAndIsDeletedFalse();
    Optional<Category> findByIdAndIsDeletedFalse(Long id);

    /** Tìm kiếm hạng mục phẳng (không phân cây) có phân trang */
    @Query("SELECT c FROM Category c " +
           "WHERE c.isDeleted = false AND " +
           "(:keyword  IS NULL OR " +
           "  LOWER(c.name)        LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "  LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:type     IS NULL OR c.type = :type) AND " +
           "(:parentId IS NULL OR c.parent.id = :parentId)")
    Page<Category> searchCategories(
            @Param("keyword")  String keyword,
            @Param("type")     CategoryType type,
            @Param("parentId") Long parentId,
            Pageable pageable
    );
}