package vn.edu.hcmuaf.fit.quanlythuchi.service.partner;

import vn.edu.hcmuaf.fit.quanlythuchi.dto.PartnerDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Partner;

import java.util.List;

public interface PartnerService {
    Partner createPartner(PartnerDTO request);
    List<Partner> getAllPartners();
    Partner getPartnerById(Long id);
    Partner updatePartner(Long id, PartnerDTO request);
    void deletePartner(Long id);
}