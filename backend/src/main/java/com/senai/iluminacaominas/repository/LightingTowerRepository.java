package com.senai.iluminacaominas.repository;

import com.senai.iluminacaominas.entity.LightingTower;
import com.senai.iluminacaominas.entity.PowerSource;
import com.senai.iluminacaominas.entity.TowerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LightingTowerRepository extends JpaRepository<LightingTower, Long> {
    Optional<LightingTower> findByCodigoIdentificacao(String codigoIdentificacao);
    boolean existsByCodigoIdentificacao(String codigoIdentificacao);
    List<LightingTower> findBySetorId(Long setorId);
    List<LightingTower> findBySetorCodigo(String codigo);
    long countByLigada(boolean ligada);
    long countByStatus(TowerStatus status);
    long countByFonteEnergia(PowerSource fonteEnergia);
}
