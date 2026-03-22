package vn.edu.hcmuaf.fit.quanlythuchi.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="roles")
@Data
public class Role {
    @Id
    private Long id;
    private String name;
    private String description;
}
