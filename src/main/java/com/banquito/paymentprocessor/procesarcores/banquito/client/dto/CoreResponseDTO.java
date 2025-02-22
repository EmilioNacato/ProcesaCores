package com.banquito.paymentprocessor.procesarcores.banquito.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "DTO para recibir la respuesta del core")
public class CoreResponseDTO {
    
    private Boolean transaccionExitosa;
    private String codigoAutorizacion;
    private String mensaje;
    private String estado;
    private String codigoError;
} 