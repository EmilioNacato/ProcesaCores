package com.banquito.paymentprocessor.procesarcores.banquito.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "DTO para la respuesta recibida del core bancario")
public class CoreResponseDTO {
    
    @Schema(description = "Indica si la transacción fue procesada exitosamente", example = "true", required = true)
    private Boolean transaccionExitosa;
    
    @Schema(description = "Código de autorización proporcionado por el core", example = "AUTH12345", required = false)
    private String codigoAutorizacion;
    
    @Schema(description = "Mensaje descriptivo del resultado de la transacción", example = "Transacción aprobada", required = true)
    private String mensaje;
    
    @Schema(description = "Estado de la transacción (APROBADA, RECHAZADA, ERROR)", example = "APROBADA", required = true)
    private String estado;
    
    @Schema(description = "Código de error en caso de rechazo o error", example = "51", required = false)
    private String codigoError;
} 