package com.banquito.paymentprocessor.procesarcores.banquito.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransaccionCoreDTO {
    private String numeroTarjeta;
    private BigDecimal monto;
    private String swiftBanco;
    private String codigoComercio;
} 