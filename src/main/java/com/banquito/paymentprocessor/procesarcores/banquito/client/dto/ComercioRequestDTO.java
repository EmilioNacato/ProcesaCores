package com.banquito.paymentprocessor.procesarcores.banquito.client.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComercioRequestDTO {
    
    private String iban;
    private String swift;
    private String tipo;
    private String codigoUnico;
    private BigDecimal monto;
    private String referencia;
} 