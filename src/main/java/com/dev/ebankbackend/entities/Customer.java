package com.dev.ebankbackend.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Entity
@Data @NoArgsConstructor @AllArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String password; // Mot de passe haché
    private String role; // Rôles séparés par des virgules, ex : "ROLE_ADMIN,ROLE_USER"
    @OneToMany(mappedBy = "customer")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<BankAccount> bankAccounts;
    @PrePersist
    private void setDefaultRole() {
        if (role == null || role.isEmpty()) {
            this.role = "USER"; // Rôle par défaut
        }
    }
}
