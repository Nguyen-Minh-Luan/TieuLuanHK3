package vn.edu.hcmuaf.fit.quanlythuchi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Partner;

import java.util.List;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, Long> {
    // Chỉ lấy ra những đối tác có isDeleted = false hoặc isDeleted = null
    List<Partner> findByIsDeletedFalseOrIsDeletedIsNull();
}