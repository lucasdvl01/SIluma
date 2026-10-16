package com.senai.iluminacaominas.controller;

import com.senai.iluminacaominas.dto.TowerDTOs.*;
import com.senai.iluminacaominas.service.AuditService;
import com.senai.iluminacaominas.service.TowerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/towers")
@RequiredArgsConstructor
@Tag(name = "Torres de Iluminação", description = "Endpoints de cadastro, monitoramento e controle operacional (RF01, RF02, RF03, RF08, RF09)")
public class TowerController {

    private final TowerService towerService;

    @GetMapping
    @Operation(summary = "Lista todas as torres de iluminação cadastradas com status e localização")
    public ResponseEntity<List<TowerResponseDTO>> getAllTowers() {
        return ResponseEntity.ok(towerService.getAllTowers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca detalhes de uma torre por ID")
    public ResponseEntity<TowerResponseDTO> getTowerById(@PathVariable Long id) {
        return ResponseEntity.ok(towerService.getTowerById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cadastra nova torre de iluminação (Exclusivo ADMIN - RF01, RNF-A05)")
    public ResponseEntity<TowerResponseDTO> createTower(@Valid @RequestBody TowerCreateDTO dto,
                                                        Authentication auth,
                                                        HttpServletRequest request) {
        String email = auth != null ? auth.getName() : "ADMIN";
        String ip = AuditService.getClientIp(request);
        return ResponseEntity.ok(towerService.createTower(dto, email, ip));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza cadastro de uma torre (Exclusivo ADMIN)")
    public ResponseEntity<TowerResponseDTO> updateTower(@PathVariable Long id,
                                                        @Valid @RequestBody TowerUpdateDTO dto,
                                                        Authentication auth,
                                                        HttpServletRequest request) {
        String email = auth != null ? auth.getName() : "ADMIN";
        String ip = AuditService.getClientIp(request);
        return ResponseEntity.ok(towerService.updateTower(id, dto, email, ip));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Exclui torre de iluminação (Exclusivo ADMIN)")
    public ResponseEntity<Map<String, String>> deleteTower(@PathVariable Long id,
                                                           Authentication auth,
                                                           HttpServletRequest request) {
        String email = auth != null ? auth.getName() : "ADMIN";
        String ip = AuditService.getClientIp(request);
        towerService.deleteTower(id, email, ip);
        return ResponseEntity.ok(Map.of("mensagem", "Torre excluída com sucesso"));
    }

    @PostMapping("/{id}/action")
    @Operation(summary = "Executa ação operacional na torre: LIGAR, DESLIGAR, MODO (RF03)")
    public ResponseEntity<TowerResponseDTO> executeAction(@PathVariable Long id,
                                                          @Valid @RequestBody TowerActionDTO actionDTO,
                                                          Authentication auth,
                                                          HttpServletRequest request) {
        String email = auth != null ? auth.getName() : "OPERADOR";
        String ip = AuditService.getClientIp(request);
        return ResponseEntity.ok(towerService.executeAction(id, actionDTO, email, ip));
    }

    @PostMapping("/batch")
    @Operation(summary = "Executa comando operacional em lote por setor da mina (RF09)")
    public ResponseEntity<List<TowerResponseDTO>> executeBatchAction(@Valid @RequestBody BatchActionDTO batchDTO,
                                                                     Authentication auth,
                                                                     HttpServletRequest request) {
        String email = auth != null ? auth.getName() : "OPERADOR";
        String ip = AuditService.getClientIp(request);
        return ResponseEntity.ok(towerService.executeBatchAction(batchDTO, email, ip));
    }
}
