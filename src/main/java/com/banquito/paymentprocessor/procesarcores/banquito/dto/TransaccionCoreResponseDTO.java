package com.banquito.paymentprocessor.procesarcores.banquito.dto;

import lombok.Data;

@Data
public class TransaccionCoreResponseDTO {
    private String estado;
    private String codigoRespuesta;
    private String mensaje;
    private String swiftBanco;
} 