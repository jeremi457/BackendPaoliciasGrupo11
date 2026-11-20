package cl.paolicias.auth.services;

import cl.paolicias.auth.dto.LoginRequest;
import cl.paolicias.auth.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}