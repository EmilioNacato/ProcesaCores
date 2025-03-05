package com.banquito.paymentprocessor.procesarcores.banquito.controller.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionCoreResponseDTO {
    
    private String codigoUnico;
    private LocalDateTime fechaProceso;
    private String estado;
    private String mensaje;
    private String codigoRespuesta;
    private String swiftBanco;
} 