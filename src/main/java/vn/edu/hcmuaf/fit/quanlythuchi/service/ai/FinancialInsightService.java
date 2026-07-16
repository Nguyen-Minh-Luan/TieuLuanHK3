package vn.edu.hcmuaf.fit.quanlythuchi.service.ai;

import vn.edu.hcmuaf.fit.quanlythuchi.dto.ai.AIInsightResponseDTO;

public interface FinancialInsightService {
    AIInsightResponseDTO getFinancialInsight(int monthsBack);
}
