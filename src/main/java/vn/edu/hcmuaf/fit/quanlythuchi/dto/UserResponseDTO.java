    package vn.edu.hcmuaf.fit.quanlythuchi.dto;

    import lombok.*;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class UserResponseDTO {
        private String username;
        private String fullName;
        private String token;
        private String status;
        private String message;
    }


