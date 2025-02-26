package com.banquito.paymentprocessor.procesarcores.banquito.dto;

import lombok.Data;

@Data
public class CoreResponse {
    private boolean aprobada;
    private String mensaje;
    private String codigoRespuesta;
    private String swiftBanco;
} 