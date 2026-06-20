package vn.edu.hcmuaf.fit.quanlythuchi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Fund;

import java.util.Optional;

@Repository
public interface FundRepository extends JpaRepository<Fund, Long> {
    @Query("SELECT SUM(f.currentBalance) FROM Fund f WHERE f.isDeleted = false")
    Optional<Double> getTotalFundBalance();

    /** Tìm kiếm quỹ tiền có phân trang, hỗ trợ keyword, type, status */
    @Query("SELECT f FROM Fund f " +
           "WHERE f.isDeleted = false AND " +
           "(:keyword IS NULL OR " +
           "  LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "  LOWER(f.type) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:type   IS NULL OR LOWER(f.type)   = LOWER(:type)) AND " +
           "(:status IS NULL OR LOWER(f.status) = LOWER(:status))")
    Page<Fund> searchFunds(
            @Param("keyword") String keyword,
            @Param("type")    String type,
            @Param("status")  String status,
            Pageable pageable
    );
}
