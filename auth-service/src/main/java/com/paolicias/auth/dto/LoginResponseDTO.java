package com.paolicias.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDTO {
    private boolean success;
    private String message;
    private String nombre;
    private boolean esAdmin;
}