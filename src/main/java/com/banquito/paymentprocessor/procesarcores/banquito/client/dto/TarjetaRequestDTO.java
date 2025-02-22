package com.banquito.paymentprocessor.procesarcores.banquito.client.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "DTO para enviar la solicitud de procesamiento de tarjeta al core")
public class TarjetaRequestDTO {
    
    private String numeroTarjeta;
    private String cvv;
    private LocalDateTime fechaCaducidad;
    private BigDecimal monto;
    private String codigoUnico;
    private String tipo;
    private String swiftBanco;
    private String referencia;
    
    // Campos específicos para diferidos
    private String codigoGateway;
    private Integer mesesDiferido;
    private String tipoDiferido;
} 