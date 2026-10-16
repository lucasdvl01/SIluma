package com.senai.iluminacaominas.controller;

import com.senai.iluminacaominas.dto.AuthDTOs.*;
import com.senai.iluminacaominas.entity.User;
import com.senai.iluminacaominas.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints de login e autenticação com JWT (RF07, RF11)")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login com e-mail e senha")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest,
                                              HttpServletRequest request) {
        return ResponseEntity.ok(authService.login(loginRequest, request));
    }

    @PostMapping("/register")
    @Operation(summary = "Cadastro de novo usuário do sistema")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest,
                                                 HttpServletRequest request) {
        return ResponseEntity.ok(authService.register(registerRequest, request));
    }

    @GetMapping("/me")
    @Operation(summary = "Obtém dados do usuário atualmente autenticado")
    public ResponseEntity<?> getMe(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("erro", "Não autenticado"));
        }
        User user = authService.getCurrentUserEntity(authentication);
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "nome", user.getNome(),
                "email", user.getEmail(),
                "perfil", user.getPerfil().name(),
                "ativo", user.isAtivo()
        ));
    }
}
