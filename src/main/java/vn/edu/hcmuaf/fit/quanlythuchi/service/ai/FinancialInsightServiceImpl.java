package vn.edu.hcmuaf.fit.quanlythuchi.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.ai.AIInsightResponseDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.ai.CategoryTrendDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.ai.DebtAlertDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.ai.FinancialSnapshotDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Debt;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.DebtRepository;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.TransactionRepository;
import vn.edu.hcmuaf.fit.quanlythuchi.service.ai.prompt.FinancialPromptBuilder;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancialInsightServiceImpl implements FinancialInsightService {

    private final TransactionRepository transactionRepository;
    private final DebtRepository debtRepository;
    private final GeminiClient geminiClient;
    private final FinancialPromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    @Value("${insight.spike-threshold-warning:0.20}")
    private double spikeThreshold;

    @Override
    @Cacheable(value = "aiInsightCache", key = "#monthsBack")
    public AIInsightResponseDTO getFinancialInsight(int monthsBack) {
        try {
            // 1. Chuẩn bị dữ liệu
            FinancialSnapshotDTO snapshot = buildFinancialSnapshot(monthsBack);

            // 2. Build Prompt
            String systemPrompt = promptBuilder.buildSystemPrompt();
            String dataPrompt = promptBuilder.buildDataPrompt(snapshot);

            // 3. Gọi Gemini
            String jsonResponse = geminiClient.generateContent(systemPrompt, dataPrompt);

            // 4. Parse Response
            if (jsonResponse != null && !jsonResponse.trim().isEmpty()) {
                // Gemini might wrap JSON in markdown code blocks like ```json ... ```
                jsonResponse = cleanJsonResponse(jsonResponse);
                AIInsightResponseDTO response = objectMapper.readValue(jsonResponse, AIInsightResponseDTO.class);
                response.setStatus("SUCCESS");
                return response;
            }
        } catch (Exception e) {
            System.err.println("Error generating AI insight: " + e.getMessage());
            e.printStackTrace();
        }

        // 5. Fallback nếu Gemini lỗi
        return buildDegradedResponse();
    }

    private String cleanJsonResponse(String response) {
        response = response.trim();
        if (response.startsWith("```json")) {
            response = response.substring(7);
        } else if (response.startsWith("```")) {
            response = response.substring(3);
        }
        if (response.endsWith("```")) {
            response = response.substring(0, response.length() - 3);
        }
        return response.trim();
    }

    private FinancialSnapshotDTO buildFinancialSnapshot(int monthsBack) {
        FinancialSnapshotDTO snapshot = new FinancialSnapshotDTO();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -(monthsBack - 1));
        String startPeriod = format.format(cal.getTime());
        String endPeriod = format.format(new Date());
        snapshot.setPeriod(startPeriod + " → " + endPeriod);

        // A. Cash Flow
        List<Object[]> cashFlowData = transactionRepository.getMonthlyCashFlowForLastNMonths(monthsBack - 1);
        List<Map<String, Object>> monthlyCashFlow = new ArrayList<>();
        double totalIncome = 0.0;
        double totalExpense = 0.0;

        for (Object[] row : cashFlowData) {
            int year = (Integer) row[0];
            int month = (Integer) row[1];
            double inc = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
            double exp = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;

            totalIncome += inc;
            totalExpense += exp;

            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", String.format("%04d-%02d", year, month));
            monthData.put("income", inc);
            monthData.put("expense", exp);
            monthlyCashFlow.add(monthData);
        }
        snapshot.setMonthlyCashFlow(monthlyCashFlow);

        // B. Summary
        Map<String, Double> summary = new HashMap<>();
        summary.put("totalIncome", totalIncome);
        summary.put("totalExpense", totalExpense);
        summary.put("netBalance", totalIncome - totalExpense);
        snapshot.setSummary(summary);

        // C. Category Spikes
        List<Object[]> categoryData = transactionRepository.getMonthlyTotalExpenseAllCategoriesForLastNMonths(monthsBack - 1);
        
        // Nhóm dữ liệu theo CategoryID
        // Map<CategoryId, Map<MonthKey, Total>>
        Map<Long, String> categoryNames = new HashMap<>();
        Map<Long, Map<String, Double>> categoryTotals = new HashMap<>();
        
        for (Object[] row : categoryData) {
            Long catId = ((Number) row[0]).longValue();
            String catName = (String) row[1];
            int year = (Integer) row[2];
            int month = (Integer) row[3];
            double total = row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;

            categoryNames.put(catId, catName);
            categoryTotals.computeIfAbsent(catId, k -> new HashMap<>()).put(String.format("%04d-%02d", year, month), total);
        }

        String currentMonthKey = String.format("%04d-%02d", LocalDate.now().getYear(), LocalDate.now().getMonthValue());
        List<CategoryTrendDTO> spikes = new ArrayList<>();

        for (Map.Entry<Long, Map<String, Double>> entry : categoryTotals.entrySet()) {
            Long catId = entry.getKey();
            Map<String, Double> totals = entry.getValue();

            double currentTotal = totals.getOrDefault(currentMonthKey, 0.0);
            double historySum = 0.0;
            int historyMonths = 0;

            for (Map.Entry<String, Double> monthEntry : totals.entrySet()) {
                if (!monthEntry.getKey().equals(currentMonthKey)) {
                    historySum += monthEntry.getValue();
                    historyMonths++;
                }
            }

            if (historyMonths > 0) {
                double historicalAverage = historySum / historyMonths;
                if (historicalAverage > 0) {
                    double overagePercent = (currentTotal - historicalAverage) / historicalAverage;
                    if (overagePercent >= spikeThreshold) {
                        spikes.add(CategoryTrendDTO.builder()
                                .category(categoryNames.get(catId))
                                .currentMonth(currentTotal)
                                .historicalAverage(historicalAverage)
                                .overagePercent(overagePercent * 100) // %
                                .build());
                    }
                }
            }
        }
        snapshot.setCategorySpikes(spikes);

        // D. Debt Alerts
        List<Debt> pendingDebts = debtRepository.findTop5ByDebtTypeAndIsPaidFalseAndIsDeletedFalseAndDueDateNotNullOrderByDueDateAsc("PAYABLE");
        LocalDate today = LocalDate.now();
        List<DebtAlertDTO> alerts = new ArrayList<>();
        
        for (Debt debt : pendingDebts) {
            LocalDate dueDate = debt.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            long daysUntilDue = ChronoUnit.DAYS.between(today, dueDate);
            
            // Cảnh báo nếu nợ còn dưới 7 ngày hoặc quá hạn
            if (daysUntilDue <= 7) {
                alerts.add(DebtAlertDTO.builder()
                        .partner(debt.getPartner() != null ? debt.getPartner().getName() : "Unknown")
                        .type(debt.getDebtType())
                        .remaining((debt.getTotalAmount() != null ? debt.getTotalAmount() : 0.0) 
                                 - (debt.getPaidAmount() != null ? debt.getPaidAmount() : 0.0))
                        .daysUntilDue(daysUntilDue)
                        .build());
            }
        }
        snapshot.setDebtAlerts(alerts);

        return snapshot;
    }

    private AIInsightResponseDTO buildDegradedResponse() {
        AIInsightResponseDTO response = new AIInsightResponseDTO();
        response.setStatus("DEGRADED");
        response.setCashFlowStatus("WARNING");
        response.setCashFlowNarrative("Hệ thống AI đang bảo trì. Chức năng phân tích xu hướng tạm thời sử dụng thuật toán cơ bản.");
        response.setRecommendations(List.of(
            "Vui lòng theo dõi sát các khoản nợ sắp đến hạn.",
            "Hãy kiểm tra lại ngân sách cho các danh mục có chi tiêu tăng cao."
        ));
        
        AIInsightResponseDTO.LiquidityRisk risk = new AIInsightResponseDTO.LiquidityRisk();
        risk.setHasRisk(false);
        risk.setMessage("");
        response.setLiquidityRisk(risk);
        
        response.setSpendingSpikes(new ArrayList<>());
        
        return response;
    }
}
