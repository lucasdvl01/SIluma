package com.senai.iluminacaominas.repository;

import com.senai.iluminacaominas.entity.MaintenanceOrder;
import com.senai.iluminacaominas.entity.MaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceOrderRepository extends JpaRepository<MaintenanceOrder, Long> {
    List<MaintenanceOrder> findByTorreIdOrderByDataAberturaDesc(Long torreId);
    List<MaintenanceOrder> findByStatusOrderByDataAberturaDesc(MaintenanceStatus status);
    List<MaintenanceOrder> findAllByOrderByDataAberturaDesc();
    long countByStatus(MaintenanceStatus status);
}
