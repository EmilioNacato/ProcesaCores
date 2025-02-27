package com.banquito.paymentprocessor.procesarcores.banquito.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "DTO para la respuesta del core bancario")
public class CoreResponse {
    
    @Schema(description = "Indica si la transacción fue aprobada", example = "true")
    private boolean aprobada;
    
    @Schema(description = "Mensaje descriptivo de la respuesta", example = "Transacción procesada exitosamente")
    private String mensaje;
    
    @Schema(description = "Código de respuesta del core", example = "00")
    private String codigoRespuesta;
    
    @Schema(description = "Código SWIFT del banco", example = "BANQECAA")
    private String swiftBanco;
} 