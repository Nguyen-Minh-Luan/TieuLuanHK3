package vn.edu.hcmuaf.fit.quanlythuchi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.quanlythuchi.config.ApiResponse;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.PartnerDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Partner;
import vn.edu.hcmuaf.fit.quanlythuchi.service.partner.PartnerService;

import java.util.List;

@RestController
@RequestMapping("/partners")
@RequiredArgsConstructor
public class PartnerController {

    private final PartnerService partnerService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PartnerDTO>>> getAllPartners() {
        return ApiResponse.ok(partnerService.getAllPartners());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PartnerDTO>> getPartnerById(@PathVariable Long id) {
        return ApiResponse.ok(partnerService.getPartnerById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PartnerDTO>> createPartner(@RequestBody PartnerDTO requestDTO) {
        return ApiResponse.created(partnerService.createPartner(requestDTO), "Tạo đối tác thành công");
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PartnerDTO>> updatePartner(
            @PathVariable Long id,
            @RequestBody PartnerDTO requestDTO) {
        return ApiResponse.ok(partnerService.updatePartner(id, requestDTO), "Cập nhật đối tác thành công");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePartner(@PathVariable Long id) {
        partnerService.deletePartner(id);
        return ApiResponse.ok(null, "Xóa đối tác thành công");
    }
}