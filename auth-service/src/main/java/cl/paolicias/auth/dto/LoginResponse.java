package cl.paolicias.auth.dto;

public record LoginResponse(String token, String nombre, String rol) {
}