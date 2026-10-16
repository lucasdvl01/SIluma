package com.senai.iluminacaominas.service;

import com.senai.iluminacaominas.dto.AuditDTOs.AuditResponseDTO;
import com.senai.iluminacaominas.entity.AuditLog;
import com.senai.iluminacaominas.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void registrarLog(String usuarioEmail, String acao, String entidade,
                             String entidadeId, String detalhes, String ipOrigem) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .usuarioEmail(usuarioEmail != null ? usuarioEmail : "SISTEMA")
                    .acao(acao)
                    .entidade(entidade)
                    .entidadeId(entidadeId)
                    .detalhes(detalhes)
                    .ipOrigem(ipOrigem != null ? ipOrigem : "127.0.0.1")
                    .dataHora(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);
            log.info("[AUDITORIA] Ação: {} por: {} na entidade: {} (ID: {}) - IP: {}",
                    acao, usuarioEmail, entidade, entidadeId, ipOrigem);
        } catch (Exception e) {
            log.error("Erro ao gravar log de auditoria", e);
        }
    }

    public static String getClientIp(HttpServletRequest request) {
        if (request == null) return "127.0.0.1";
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @Transactional(readOnly = true)
    public List<AuditResponseDTO> getLogsRecentes() {
        return auditLogRepository.findTop50ByOrderByDataHoraDesc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private AuditResponseDTO toDTO(AuditLog log) {
        return AuditResponseDTO.builder()
                .id(log.getId())
                .usuarioEmail(log.getUsuarioEmail())
                .acao(log.getAcao())
                .entidade(log.getEntidade())
                .entidadeId(log.getEntidadeId())
                .detalhes(log.getDetalhes())
                .ipOrigem(log.getIpOrigem())
                .dataHora(log.getDataHora())
                .build();
    }
}
