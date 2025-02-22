package com.banquito.paymentprocessor.procesarcores.banquito.client.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "DTO para enviar la solicitud de procesamiento de comercio al core")
public class ComercioRequestDTO {
    
    private String codigoUnico;
    private BigDecimal monto;
    private String swiftBancoComercio;
    private String cuentaIbanComercio;
    private String referencia;
    private String tipo;
    
    // Campos específicos para comercio
    private String nombreComercio;
    private String codigoComercio;
} 