package com.senai.iluminacaominas.controller;

import com.senai.iluminacaominas.dto.AuditDTOs.AuditResponseDTO;
import com.senai.iluminacaominas.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Auditoria & Histórico", description = "Endpoints de auditoria para rastreamento de ações operacionais e de gestão (RF12, RNF-W18)")
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista histórico de auditoria recente com usuário, timestamp, ação e IP (Exclusivo ADMIN)")
    public ResponseEntity<List<AuditResponseDTO>> getLogs() {
        return ResponseEntity.ok(auditService.getLogsRecentes());
    }
}
