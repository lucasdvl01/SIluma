package com.senai.iluminacaominas.controller;

import com.senai.iluminacaominas.dto.MaintenanceDTOs.*;
import com.senai.iluminacaominas.entity.MaintenanceStatus;
import com.senai.iluminacaominas.service.AuditService;
import com.senai.iluminacaominas.service.MaintenanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maintenances")
@RequiredArgsConstructor
@Tag(name = "Manutenções", description = "Endpoints para registro de falhas, ordens de serviço e preventiva (RF04)")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    @GetMapping
    @Operation(summary = "Lista ordens de manutenção com filtro opcional por status")
    public ResponseEntity<List<MaintenanceResponseDTO>> getAll(
            @RequestParam(required = false) MaintenanceStatus status) {
        return ResponseEntity.ok(maintenanceService.getAll(status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca detalhes de uma ordem de manutenção por ID")
    public ResponseEntity<MaintenanceResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(maintenanceService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Registra chamado de falha / abre ordem de manutenção (RF04)")
    public ResponseEntity<MaintenanceResponseDTO> create(@Valid @RequestBody MaintenanceCreateDTO dto,
                                                         Authentication auth,
                                                         HttpServletRequest request) {
        String email = auth != null ? auth.getName() : "OPERADOR";
        String ip = AuditService.getClientIp(request);
        return ResponseEntity.ok(maintenanceService.createMaintenance(dto, email, ip));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza status ou conclusão de ordem de manutenção")
    public ResponseEntity<MaintenanceResponseDTO> update(@PathVariable Long id,
                                                         @Valid @RequestBody MaintenanceUpdateDTO dto,
                                                         Authentication auth,
                                                         HttpServletRequest request) {
        String email = auth != null ? auth.getName() : "OPERADOR";
        String ip = AuditService.getClientIp(request);
        return ResponseEntity.ok(maintenanceService.updateMaintenance(id, dto, email, ip));
    }
}
