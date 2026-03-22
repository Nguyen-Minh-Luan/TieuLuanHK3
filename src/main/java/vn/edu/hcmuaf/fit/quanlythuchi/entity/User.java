package vn.edu.hcmuaf.fit.quanlythuchi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.Role;

import java.util.Date;

@Entity
@Table(name ="users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;
    @Column(unique = true,nullable = false)
    private String username;
    private String password;
    @Column(name = "full_name")
    private String fullName;
    @Column(unique = true)
    private String email;
    private Boolean isDeleted;
    private Date created_at;
    private Date update_at;
}
