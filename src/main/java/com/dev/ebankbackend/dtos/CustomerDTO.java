package com.dev.ebankbackend.dtos;
import lombok.Data;
@Data
public class CustomerDTO {
    private Long id;
    private String name;
    private String email;
    private String password;
    private String role;
}
