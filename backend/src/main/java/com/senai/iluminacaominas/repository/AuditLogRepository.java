package com.senai.iluminacaominas.repository;

import com.senai.iluminacaominas.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findAllByOrderByDataHoraDesc();
    Page<AuditLog> findAllByOrderByDataHoraDesc(Pageable pageable);
    List<AuditLog> findTop50ByOrderByDataHoraDesc();
}
