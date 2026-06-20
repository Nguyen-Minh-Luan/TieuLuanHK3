package vn.edu.hcmuaf.fit.quanlythuchi.dto;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Wrapper DTO thống nhất cho kết quả phân trang.
 * Convert từ Spring Data Page<T> sang JSON-friendly format cho Frontend.
 * Page index trả về 1-based (client gửi 1 = trang đầu tiên).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedResponseDTO<T> {
    private List<T> content;
    private int page;           // Trang hiện tại (1-based)
    private int size;           // Số phần tử mỗi trang
    private long totalElements; // Tổng số bản ghi
    private int totalPages;     // Tổng số trang
    private boolean last;       // Có phải trang cuối không

    /**
     * Tạo PagedResponseDTO từ Spring Data Page<T>.
     * Chuyển đổi page index về 1-based để nhất quán với quy ước phía Client.
     */
    public static <T> PagedResponseDTO<T> from(Page<T> page) {
        return PagedResponseDTO.<T>builder()
                .content(page.getContent())
                .page(page.getNumber() + 1) // Convert 0-based → 1-based
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
