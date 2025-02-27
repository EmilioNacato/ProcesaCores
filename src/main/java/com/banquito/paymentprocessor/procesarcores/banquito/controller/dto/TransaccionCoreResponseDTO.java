package com.banquito.paymentprocessor.procesarcores.banquito.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "DTO para la respuesta del procesamiento en el core bancario")
public class TransaccionCoreResponseDTO {
    
    @Schema(description = "Estado de la transacción", example = "APROBADA")
    private String estado;
    
    @Schema(description = "Código de respuesta del core", example = "00")
    private String codigoRespuesta;
    
    @Schema(description = "Mensaje descriptivo de la respuesta", example = "Transacción procesada exitosamente")
    private String mensaje;
    
    @Schema(description = "Código SWIFT del banco", example = "BANQECAA")
    private String swiftBanco;
} 