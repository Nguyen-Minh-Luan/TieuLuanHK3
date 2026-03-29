package vn.edu.hcmuaf.fit.quanlythuchi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String fullname;
    private String password;
}
