package com.senai.iluminacaominas.service;

import com.senai.iluminacaominas.dto.SectorDTOs.SectorCreateDTO;
import com.senai.iluminacaominas.dto.SectorDTOs.SectorResponseDTO;
import com.senai.iluminacaominas.entity.Sector;
import com.senai.iluminacaominas.repository.SectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SectorService {

    private final SectorRepository sectorRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<SectorResponseDTO> getAllSectors() {
        return sectorRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Sector getSectorEntityById(Long id) {
        return sectorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Setor não encontrado com ID: " + id));
    }

    @Transactional
    public SectorResponseDTO createSector(SectorCreateDTO dto, String userEmail, String ip) {
        Sector sector = Sector.builder()
                .codigo(dto.getCodigo())
                .nome(dto.getNome())
                .areaMina(dto.getAreaMina())
                .frentesDeLavra(dto.getFrentesDeLavra())
                .descricao(dto.getDescricao())
                .build();

        sector = sectorRepository.save(sector);

        auditService.registrarLog(
                userEmail,
                "CADASTRAR_SETOR",
                "Sector",
                sector.getId().toString(),
                "Setor cadastrado: " + sector.getNome() + " (" + sector.getCodigo() + ")",
                ip
        );

        return toDTO(sector);
    }

    public SectorResponseDTO toDTO(Sector sector) {
        int totalTorres = sector.getTorres() != null ? sector.getTorres().size() : 0;
        int ligadas = sector.getTorres() != null ? (int) sector.getTorres().stream().filter(t -> t.isLigada()).count() : 0;

        return SectorResponseDTO.builder()
                .id(sector.getId())
                .codigo(sector.getCodigo())
                .nome(sector.getNome())
                .areaMina(sector.getAreaMina())
                .frentesDeLavra(sector.getFrentesDeLavra())
                .descricao(sector.getDescricao())
                .totalTorres(totalTorres)
                .torresLigadas(ligadas)
                .build();
    }
}
