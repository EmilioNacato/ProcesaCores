package com.banquito.paymentprocessor.procesarcores.banquito.client.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "DTO para solicitud de débito a tarjeta en el core bancario")
public class TarjetaRequestDTO {
    
    @Schema(description = "Número de la tarjeta a debitar", example = "4557880123456789", required = true)
    private String numeroTarjeta;
    
    @Schema(description = "Código de seguridad de la tarjeta", example = "123", required = true)
    private String cvv;
    
    @Schema(description = "Fecha de caducidad de la tarjeta", required = true)
    private LocalDateTime fechaCaducidad;
    
    @Schema(description = "Monto a debitar de la tarjeta", example = "100.50", required = true)
    private BigDecimal monto;
    
    @Schema(description = "Código único identificador de la transacción", example = "TX123456789", required = true)
    private String codigoUnico;
    
    @Schema(description = "Tipo de transacción (COMPRA, RETIRO, etc)", example = "COMPRA", required = true)
    private String tipo;
    
    @Schema(description = "Código SWIFT del banco emisor de la tarjeta", example = "BANECUXXXX", required = true)
    private String swiftBanco;
    
    @Schema(description = "Referencia de la transacción", example = "Compra en línea", required = false)
    private String referencia;
    
    @Schema(description = "Código asignado por el gateway de pago", example = "GW987654321", required = false)
    private String codigoGateway;
    
    @Schema(description = "Número de meses para el diferido (1-48)", example = "3", required = false)
    private Integer mesesDiferido;
    
    @Schema(description = "Tipo de diferido (NORMAL, SIN_INTERESES)", example = "NORMAL", required = false)
    private String tipoDiferido;
} 