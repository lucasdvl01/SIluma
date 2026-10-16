package com.senai.iluminacaominas.repository;

import com.senai.iluminacaominas.entity.IoTTelemetry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IoTTelemetryRepository extends JpaRepository<IoTTelemetry, Long> {
    List<IoTTelemetry> findByCodigoTorreOrderByDataHoraDesc(String codigoTorre);
    Optional<IoTTelemetry> findTopByCodigoTorreOrderByDataHoraDesc(String codigoTorre);
    List<IoTTelemetry> findTop100ByOrderByDataHoraDesc();
}
