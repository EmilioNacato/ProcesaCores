package com.banquito.paymentprocessor.procesarcores.banquito.client.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "DTO para solicitud de acreditación a la cuenta del comercio")
public class ComercioRequestDTO {
    
    @Schema(description = "Código único identificador de la transacción", example = "TX123456789", required = true)
    private String codigoUnico;
    
    @Schema(description = "Monto a acreditar al comercio", example = "100.50", required = true)
    private BigDecimal monto;
    
    @Schema(description = "Código SWIFT del banco del comercio", example = "PICHEQXX", required = true)
    private String swiftBancoComercio;
    
    @Schema(description = "Cuenta IBAN del comercio donde se acreditará el monto", example = "EC11 0000 0000 0123 4567 8901", required = true)
    private String cuentaIbanComercio;
    
    @Schema(description = "Referencia de la transacción", example = "Compra en línea", required = false)
    private String referencia;
    
    @Schema(description = "Tipo de transacción (COMPRA, RETIRO, etc)", example = "COMPRA", required = true)
    private String tipo;
    
    @Schema(description = "Nombre del comercio beneficiario", example = "Mi Comercio S.A.", required = true)
    private String nombreComercio;
    
    @Schema(description = "Código identificador del comercio en el sistema", example = "COM123456", required = true)
    private String codigoComercio;
} 