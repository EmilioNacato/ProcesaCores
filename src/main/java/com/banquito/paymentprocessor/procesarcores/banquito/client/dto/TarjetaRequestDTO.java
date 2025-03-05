package com.banquito.paymentprocessor.procesarcores.banquito.client.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TarjetaRequestDTO {
    
    private String tipo;
    private BigDecimal monto;
    private String moneda;
    private String pais;
    private String swift;
    private String numeroTarjeta;
    private String codigoUnicoTransaccion;
    private String referencia;
    private String transaccionEncriptada;
    private Boolean diferido;
    private Integer cuotas;
} 