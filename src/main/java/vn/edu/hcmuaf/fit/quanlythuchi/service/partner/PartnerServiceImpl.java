package vn.edu.hcmuaf.fit.quanlythuchi.service.partner;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.PartnerDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Partner;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.PartnerRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PartnerServiceImpl implements PartnerService {

    private final PartnerRepository partnerRepository;

    @Override
    @Transactional
    public Partner createPartner(PartnerDTO request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đối tác không được để trống!");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email đối tác không được để trống!");
        }

        Partner partner = new Partner();
        partner.setName(request.getName());
        partner.setType(request.getType());
        partner.setEmail(request.getEmail());

        // Mặc định khi tạo mới thì isDeleted = false
        partner.setIsDeleted(false);

        return partnerRepository.save(partner);
    }

    @Override
    public List<Partner> getAllPartners() {
        // Chỉ trả về danh sách đối tác đang hoạt động (chưa bị xóa)
        return partnerRepository.findByIsDeletedFalseOrIsDeletedIsNull();
    }

    @Override
    public Partner getPartnerById(Long id) {
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Đối tác với ID: " + id));

        // Kiểm tra thêm: Nếu đối tác đã bị xóa mềm thì ném lỗi không tìm thấy
        if (Boolean.TRUE.equals(partner.getIsDeleted())) {
            throw new RuntimeException("Đối tác này không tồn tại hoặc đã bị xóa!");
        }

        return partner;
    }

    @Override
    @Transactional
    public Partner updatePartner(Long id, PartnerDTO request) {
        // Lấy đối tác lên (đã bao gồm logic kiểm tra isDeleted)
        Partner partner = getPartnerById(id);

        // 1. Chỉ cập nhật Tên (name) nếu request có gửi lên (khác null)
        if (request.getName() != null) {
            if (request.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Tên đối tác không được là chuỗi rỗng!");
            }
            partner.setName(request.getName());
        }

        // 2. Chỉ cập nhật Email nếu request có gửi lên (khác null)
        if (request.getEmail() != null) {
            if (request.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("Email đối tác không được là chuỗi rỗng!");
            }
            partner.setEmail(request.getEmail());
        }

        // 3. Chỉ cập nhật Loại (type) nếu request có gửi lên
        if (request.getType() != null) {
            partner.setType(request.getType());
        }

        // Nhờ có @Transactional và cơ chế Dirty Checking của Hibernate,
        // nó sẽ tự động so sánh và chỉ thực thi lệnh UPDATE dưới DB
        // nếu thực sự có giá trị nào đó bị thay đổi.
        return partnerRepository.save(partner);
    }

    @Override
    @Transactional
    public void deletePartner(Long id) {
        // Lấy đối tác lên (hàm này đã kiểm tra ngoại lệ nếu ID không có hoặc đã xóa)
        Partner partner = getPartnerById(id);

        // --- LOGIC XÓA MỀM ---
        partner.setIsDeleted(true);

        // Cập nhật lại vào DB thay vì xóa cứng
        partnerRepository.save(partner);
    }
}