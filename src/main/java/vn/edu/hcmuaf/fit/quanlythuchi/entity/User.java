package vn.edu.hcmuaf.fit.quanlythuchi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name ="users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true,nullable = false)
    private String username;
    private String password;
    private Integer role;
    @Column(name = "full_name")
    private String fullName;
    @Column(unique = true)
    private String email;
    private Boolean isDeleted;
    private Date created_at;
    private Date update_at;
    private String token;
}
