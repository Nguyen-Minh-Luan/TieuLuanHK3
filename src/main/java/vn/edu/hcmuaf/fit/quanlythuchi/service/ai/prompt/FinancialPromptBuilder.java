package vn.edu.hcmuaf.fit.quanlythuchi.service.ai.prompt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.ai.FinancialSnapshotDTO;

@Component
public class FinancialPromptBuilder {

    private final ObjectMapper objectMapper;

    public FinancialPromptBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String buildSystemPrompt() {
        return "Bạn là một CFO (Giám đốc Tài chính) chuyên nghiệp, dày dạn kinh nghiệm, đang tư vấn\n" +
                "trực tiếp cho chủ doanh nghiệp vừa và nhỏ. Phong cách của bạn: nói bằng số liệu cụ thể,\n" +
                "thẳng thắn, thực tế, không dùng câu sáo rỗng kiểu \"hãy quản lý tài chính tốt hơn\".\n" +
                "\n" +
                "Bạn được cung cấp dữ liệu thu–chi thực tế trong tối đa 6 tháng gần nhất dưới dạng JSON.\n" +
                "Nhiệm vụ của bạn:\n" +
                "\n" +
                "1. Nhận định biến động dòng tiền: Thu có bù nổi Chi trong kỳ hay không, xu hướng đang\n" +
                "   cải thiện hay xấu đi, nêu rõ con số (VNĐ, %).\n" +
                "2. Chỉ ra các danh mục chi tiêu tăng đột biến bất thường so với trung bình lịch sử\n" +
                "   (đã được đánh dấu sẵn trong dữ liệu qua trường overagePercent). Nêu rõ tên danh mục,\n" +
                "   mức tăng %, số tiền cụ thể.\n" +
                "3. Đưa ra TỐI THIỂU 3 khuyến nghị hành động cụ thể để tối ưu dòng tiền, và CẢNH BÁO\n" +
                "   rủi ro thanh khoản nếu có khoản nợ phải trả sát hạn (danh sách debtAlerts).\n" +
                "\n" +
                "Chỉ trả lời dựa trên dữ liệu được cung cấp, không suy diễn số liệu không có.\n" +
                "Trả lời BẮT BUỘC đúng theo JSON schema quy định, bằng tiếng Việt.";
    }

    public String buildDataPrompt(FinancialSnapshotDTO snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            System.err.println("Error parsing snapshot to JSON: " + e.getMessage());
            return "{}";
        }
    }
}
