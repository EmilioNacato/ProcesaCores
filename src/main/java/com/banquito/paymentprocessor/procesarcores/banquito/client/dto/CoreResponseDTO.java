package com.banquito.paymentprocessor.procesarcores.banquito.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoreResponseDTO {
    
    private String estado;
    private String mensaje;
    private String codigoRespuesta;
    private String codigoTransaccion;
} 