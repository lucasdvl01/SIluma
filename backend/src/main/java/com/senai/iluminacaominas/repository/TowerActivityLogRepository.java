package com.senai.iluminacaominas.repository;

import com.senai.iluminacaominas.entity.TowerActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TowerActivityLogRepository extends JpaRepository<TowerActivityLog, Long> {
    List<TowerActivityLog> findByTorreIdOrderByDataHoraLigouDesc(Long torreId);
    Optional<TowerActivityLog> findTopByTorreIdAndDataHoraDesligouIsNullOrderByDataHoraLigouDesc(Long torreId);

    @Query("SELECT COALESCE(SUM(l.minutosAtivo), 0) FROM TowerActivityLog l")
    Long sumTotalMinutosAtivo();

    @Query("SELECT COALESCE(SUM(l.consumoEstimadoKWh), 0.0) FROM TowerActivityLog l")
    Double sumTotalConsumoKWh();
}
