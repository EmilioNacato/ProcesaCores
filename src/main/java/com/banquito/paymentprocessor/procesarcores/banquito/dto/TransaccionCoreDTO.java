package com.banquito.paymentprocessor.procesarcores.banquito.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransaccionCoreDTO {
    private String numeroTarjeta;
    private String cvv;
    private String fechaCaducidad;
    private BigDecimal monto;
    private String codigoUnico;
    private String tipo;
    private String swiftBanco;
    private String referencia;
    private String codigoGateway;
    private Integer mesesDiferido;
    private String tipoDiferido;
    private String codigoComercio;
    private String swiftBancoComercio;
    private String cuentaIbanComercio;
} 