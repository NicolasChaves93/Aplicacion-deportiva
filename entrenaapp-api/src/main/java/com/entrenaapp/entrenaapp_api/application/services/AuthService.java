package com.entrenaapp.entrenaapp_api.application.services;

import com.entrenaapp.entrenaapp_api.application.dto.AuthResponse;
import com.entrenaapp.entrenaapp_api.application.dto.LoginRequest;
import com.entrenaapp.entrenaapp_api.application.dto.RegistroRequest;
import com.entrenaapp.entrenaapp_api.domain.model.Usuario;
import com.entrenaapp.entrenaapp_api.infrastructure.config.JwtService;
import com.entrenaapp.entrenaapp_api.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse registrar(RegistroRequest request) {
        String email = normalizarEmail(request.email());
        if (usuarioRepository.existsByEmail(email)) {
            throw new RuntimeException("Ya existe un usuario con ese email");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.nombre())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();

        Usuario guardado = usuarioRepository.save(usuario);
        return generarRespuesta(guardado);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(normalizarEmail(request.email()))
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        return generarRespuesta(usuario);
    }

    private String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private AuthResponse generarRespuesta(Usuario usuario) {
        String token = jwtService.generarToken(usuario.getEmail(), usuario.getId(), usuario.getRol());
        return new AuthResponse(token, usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getRol());
    }
}
