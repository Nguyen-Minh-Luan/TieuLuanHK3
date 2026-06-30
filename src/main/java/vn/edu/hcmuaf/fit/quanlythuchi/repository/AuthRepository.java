package vn.edu.hcmuaf.fit.quanlythuchi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.User;

import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    /** Tìm kiếm người dùng có phân trang, hỗ trợ keyword, lọc theo role và status */
    @Query("SELECT u FROM User u " +
           "WHERE (u.isDeleted IS NULL OR u.isDeleted = false) AND " +
           "(:keyword IS NULL OR " +
           "  LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "  LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "  LOWER(u.email)    LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:status IS NULL OR u.status = :status)")
    Page<User> searchUsers(
            @Param("keyword") String keyword,
            @Param("role")    Integer role,
            @Param("status")  String status,
            Pageable pageable
    );
}
