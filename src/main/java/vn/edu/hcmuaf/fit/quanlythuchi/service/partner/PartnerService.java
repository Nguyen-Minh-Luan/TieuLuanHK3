package vn.edu.hcmuaf.fit.quanlythuchi.service.partner;

import org.springframework.data.domain.Page;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.PartnerDTO;

public interface PartnerService {
    PartnerDTO createPartner(PartnerDTO request);

    /**
     * Lấy danh sách đối tác với phân trang và tìm kiếm.
     * @param keyword  Tìm theo name, email, address (LIKE %keyword%)
     * @param type     Lọc theo loại đối tác (CUSTOMER, SUPPLIER...)
     * @param page     Số trang (1-based)
     * @param size     Số phần tử mỗi trang
     * @param sortBy   Tên field để sắp xếp (mặc định "name")
     * @param sortDir  Chiều sắp xếp: "asc" hoặc "desc"
     */
    Page<PartnerDTO> getAllPartners(String keyword, String type,
                                   int page, int size, String sortBy, String sortDir);

    PartnerDTO getPartnerById(Long id);
    PartnerDTO updatePartner(Long id, PartnerDTO request);
    void deletePartner(Long id);
}