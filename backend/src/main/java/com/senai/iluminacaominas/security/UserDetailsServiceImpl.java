package com.senai.iluminacaominas.security;

import com.senai.iluminacaominas.entity.User;
import com.senai.iluminacaominas.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String query = email != null ? email.trim() : "";
        if ("admin".equalsIgnoreCase(query)) {
            query = "admin@mineracao.com.br";
        } else if ("operador".equalsIgnoreCase(query)) {
            query = "operador@mineracao.com.br";
        }
        final String targetEmail = query;
        User user = userRepository.findByEmail(targetEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com e-mail: " + email));
        return UserPrincipal.create(user);
    }
}
