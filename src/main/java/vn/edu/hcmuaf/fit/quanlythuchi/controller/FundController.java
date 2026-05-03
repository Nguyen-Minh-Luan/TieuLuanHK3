package vn.edu.hcmuaf.fit.quanlythuchi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.FundResponseDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Fund;
import vn.edu.hcmuaf.fit.quanlythuchi.service.fund.FundService;

import java.util.Optional;

@RestController
@RequestMapping("/funds")
@RequiredArgsConstructor
public class FundController {

    private final FundService fundService;

    @PostMapping
    public ResponseEntity<?> createFund(@RequestBody FundResponseDTO fund) {
        try {
            Fund createdFund = fundService.createFund(fund);
            return new ResponseEntity<Fund>(createdFund,HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<String>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateFund(@PathVariable Long id, @RequestBody FundResponseDTO fundDTO) {
        try {
            FundResponseDTO frdto = new FundResponseDTO();
            Fund updatedFund = fundService.updateFund(id, fundDTO);
            frdto.setName(updatedFund.getName());
            frdto.setType(updatedFund.getType());
            frdto.setStatus(updatedFund.getStatus());
            frdto.setInitialBalance(updatedFund.getInitialBalance());
            return ResponseEntity.ok(frdto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFund(@PathVariable Long id) {
        try {
            fundService.deleteFund(id);
            return ResponseEntity.ok("Xóa nguồn tiền thành công!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    @GetMapping("/total-balance")
    public ResponseEntity<?> getTotalFundBalance() {
        try {
            // Gọi service với tên hàm tương ứng
            Double totalBalance = fundService.getTotalFund();

            // Trả về kết quả dưới dạng Map để Frontend nhận được JSON: { "totalBalance": ... }
            java.util.Map<String, Double> response = new java.util.HashMap<>();
            response.put("totalBalance", totalBalance);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Trả về lỗi nếu có vấn đề hệ thống
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}