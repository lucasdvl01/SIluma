package com.senai.iluminacaominas.controller;

import com.senai.iluminacaominas.dto.SectorDTOs.*;
import com.senai.iluminacaominas.service.AuditService;
import com.senai.iluminacaominas.service.SectorService;
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

@RestController
@RequestMapping("/api/sectors")
@RequiredArgsConstructor
@Tag(name = "Setores da Mina", description = "Endpoints de gerenciamento dos trechos e frentes de lavra")
public class SectorController {

    private final SectorService sectorService;

    @GetMapping
    @Operation(summary = "Lista todos os setores da mina")
    public ResponseEntity<List<SectorResponseDTO>> getAllSectors() {
        return ResponseEntity.ok(sectorService.getAllSectors());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cadastra novo setor ou trecho da mina (Exclusivo ADMIN)")
    public ResponseEntity<SectorResponseDTO> createSector(@Valid @RequestBody SectorCreateDTO dto,
                                                          Authentication auth,
                                                          HttpServletRequest request) {
        String email = auth != null ? auth.getName() : "ADMIN";
        String ip = AuditService.getClientIp(request);
        return ResponseEntity.ok(sectorService.createSector(dto, email, ip));
    }
}
