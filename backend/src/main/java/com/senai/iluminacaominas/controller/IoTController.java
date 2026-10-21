package com.senai.iluminacaominas.controller;

import com.senai.iluminacaominas.dto.IoTTelemetryDTOs.*;
import com.senai.iluminacaominas.service.AuditService;
import com.senai.iluminacaominas.service.IoTService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/iot")
@RequiredArgsConstructor
@Tag(name = "IoT & Arduino", description = "Endpoints de integração com o circuito Arduino Mega e simuladores (RF06, RNF-C10)")
public class IoTController {

    private final IoTService ioTService;

    @PostMapping("/telemetry")
    @Operation(summary = "Recebe telemetria encaminhada pelo circuito Arduino via HTTP POST (RF06)")
    public ResponseEntity<TelemetryResponseDTO> receiveTelemetry(
            @Valid @RequestBody TelemetryRequestDTO telemetryRequest,
            HttpServletRequest request) {
        String ip = AuditService.getClientIp(request);
        return ResponseEntity.ok(ioTService.processTelemetry(telemetryRequest, ip));
    }

    @GetMapping("/command/{codigoTorre}")
    @Operation(summary = "Polling periódico do dispositivo físico para obter instrução remota atual")
    public ResponseEntity<HardwarePollingDTO> pollCommand(
            @PathVariable String codigoTorre) {
        return ResponseEntity.ok(ioTService.getHardwarePolling(codigoTorre));
    }
}
