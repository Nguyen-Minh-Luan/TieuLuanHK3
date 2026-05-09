package vn.edu.hcmuaf.fit.quanlythuchi.service.partner;

import vn.edu.hcmuaf.fit.quanlythuchi.dto.PartnerDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Partner;

import java.util.List;

public interface PartnerService {
    PartnerDTO createPartner(PartnerDTO request);
    List<PartnerDTO> getAllPartners();
    PartnerDTO getPartnerById(Long id);
    PartnerDTO updatePartner(Long id, PartnerDTO request);
    void deletePartner(Long id);
}