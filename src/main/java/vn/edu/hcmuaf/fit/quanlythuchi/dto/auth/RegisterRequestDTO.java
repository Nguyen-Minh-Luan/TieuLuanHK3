package vn.edu.hcmuaf.fit.quanlythuchi.dto.auth;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {
    private String username;
    private String password;
    private String fullName;
    private String email;
}