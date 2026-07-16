package vn.edu.hcmuaf.fit.quanlythuchi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.hcmuaf.fit.quanlythuchi.config.ApiResponse;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.ai.AIInsightResponseDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.service.ai.FinancialInsightService;

@RestController
@RequestMapping("/reports/ai-insights")
@RequiredArgsConstructor
public class AIInsightController {

    private final FinancialInsightService financialInsightService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TONGHOP')")
    public ResponseEntity<ApiResponse<AIInsightResponseDTO>> getAIInsights(
            @RequestParam(value = "months", defaultValue = "6") int months,
            @RequestParam(value = "forceRefresh", required = false, defaultValue = "false") boolean forceRefresh) {

        if (months > 6) {
            months = 6;
        } else if (months < 1) {
            months = 1;
        }

        AIInsightResponseDTO response = financialInsightService.getFinancialInsight(months);
        return ApiResponse.ok(response, "Phân tích AI thành công");
    }
}
