package com.senai.iluminacaominas.service;

import com.senai.iluminacaominas.dto.AuthDTOs.AuthResponse;
import com.senai.iluminacaominas.dto.AuthDTOs.LoginRequest;
import com.senai.iluminacaominas.dto.AuthDTOs.RegisterRequest;
import com.senai.iluminacaominas.entity.Role;
import com.senai.iluminacaominas.entity.User;
import com.senai.iluminacaominas.repository.UserRepository;
import com.senai.iluminacaominas.security.JwtTokenProvider;
import com.senai.iluminacaominas.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuditService auditService;

    @Transactional
    public AuthResponse login(LoginRequest loginRequest, HttpServletRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getSenha()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        String ip = AuditService.getClientIp(request);
        auditService.registrarLog(
                userPrincipal.getUsername(),
                "LOGIN_SUCESSO",
                "User",
                userPrincipal.getId().toString(),
                "Usuário autenticado com sucesso",
                ip
        );

        String role = userPrincipal.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("OPERADOR");

        return AuthResponse.builder()
                .token(token)
                .tipo("Bearer")
                .email(userPrincipal.getUsername())
                .nome(userPrincipal.getNome())
                .perfil(role)
                .expiresIn(86400000L) // 24h
                .build();
    }

    @Transactional
    public AuthResponse register(RegisterRequest registerRequest, HttpServletRequest request) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("E-mail já cadastrado no sistema");
        }

        User user = User.builder()
                .nome(registerRequest.getNome())
                .email(registerRequest.getEmail())
                .senha(passwordEncoder.encode(registerRequest.getSenha()))
                .perfil(registerRequest.getPerfil() != null ? registerRequest.getPerfil() : Role.OPERADOR)
                .ativo(true)
                .build();

        user = userRepository.save(user);

        String ip = AuditService.getClientIp(request);
        auditService.registrarLog(
                user.getEmail(),
                "CADASTRO_USUARIO",
                "User",
                user.getId().toString(),
                "Novo usuário registrado com perfil: " + user.getPerfil(),
                ip
        );

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registerRequest.getEmail(),
                        registerRequest.getSenha()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .token(token)
                .tipo("Bearer")
                .email(user.getEmail())
                .nome(user.getNome())
                .perfil(user.getPerfil().name())
                .expiresIn(86400000L)
                .build();
    }

    @Transactional(readOnly = true)
    public User getCurrentUserEntity(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Usuário não está autenticado");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário logado não encontrado no banco"));
    }
}
