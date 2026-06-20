package vn.edu.hcmuaf.fit.quanlythuchi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.quanlythuchi.config.ApiResponse;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.PagedResponseDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.PartnerDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.service.partner.PartnerService;

@RestController
@RequestMapping("/partners")
@RequiredArgsConstructor
public class PartnerController {

    private final PartnerService partnerService;

    /** GET /partners — Lấy danh sách đối tác có phân trang và tìm kiếm */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponseDTO<PartnerDTO>>> getAllPartners(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1")    int page,
            @RequestParam(defaultValue = "10")   int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc")  String sortDir) {
        return ApiResponse.ok(
                PagedResponseDTO.from(
                        partnerService.getAllPartners(keyword, type, page, size, sortBy, sortDir)));
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