package vn.edu.hcmuaf.fit.quanlythuchi.service.partner;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.PartnerDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Partner;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.PartnerRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartnerServiceImpl implements PartnerService {

    private final PartnerRepository partnerRepository;

    @Override
    @Transactional
    public PartnerDTO createPartner(PartnerDTO request) {
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

        return toDTO(partnerRepository.save(partner));
    }

    @Override
    public List<PartnerDTO> getAllPartners() {
        return partnerRepository.findByIsDeletedFalseOrIsDeletedIsNull()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PartnerDTO getPartnerById(Long id) {
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Đối tác với ID: " + id));

        if (Boolean.TRUE.equals(partner.getIsDeleted())) {
            throw new RuntimeException("Đối tác này không tồn tại hoặc đã bị xóa!");
        }

        return toDTO(partner);
    }

    @Override
    @Transactional
    public PartnerDTO updatePartner(Long id, PartnerDTO request) {
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Đối tác với ID: " + id));

        if (Boolean.TRUE.equals(partner.getIsDeleted())) {
            throw new RuntimeException("Đối tác này không tồn tại hoặc đã bị xóa!");
        }

        if (request.getName() != null) {
            if (request.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Tên đối tác không được là chuỗi rỗng!");
            }
            partner.setName(request.getName());
        }

        if (request.getEmail() != null) {
            if (request.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("Email đối tác không được là chuỗi rỗng!");
            }
            partner.setEmail(request.getEmail());
        }

        if (request.getType() != null) {
            partner.setType(request.getType());
        }

        return toDTO(partnerRepository.save(partner));
    }

    @Override
    @Transactional
    public void deletePartner(Long id) {
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Đối tác với ID: " + id));

        if (Boolean.TRUE.equals(partner.getIsDeleted())) {
            throw new RuntimeException("Đối tác này đã bị xóa trước đó!");
        }

        partner.setIsDeleted(true);
        partnerRepository.save(partner);
    }


    private PartnerDTO toDTO(Partner partner) {
        return PartnerDTO.builder()
                .id(partner.getId())
                .name(partner.getName())
                .type(partner.getType())
                .email(partner.getEmail())
                .build();
    }
}