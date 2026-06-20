package vn.edu.hcmuaf.fit.quanlythuchi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Partner;

import java.util.List;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, Long> {
    // Chỉ lấy ra những đối tác có isDeleted = false hoặc isDeleted = null
    List<Partner> findByIsDeletedFalseOrIsDeletedIsNull();

    /** Tìm kiếm đối tác có phân trang, hỗ trợ keyword và lọc theo type */
    @Query("SELECT p FROM Partner p " +
           "WHERE (p.isDeleted IS NULL OR p.isDeleted = false) AND " +
           "(:keyword IS NULL OR " +
           "  LOWER(p.name)    LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "  LOWER(p.email)   LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "  LOWER(p.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:type IS NULL OR LOWER(p.type) = LOWER(:type))")
    Page<Partner> searchPartners(
            @Param("keyword") String keyword,
            @Param("type")    String type,
            Pageable pageable
    );
}