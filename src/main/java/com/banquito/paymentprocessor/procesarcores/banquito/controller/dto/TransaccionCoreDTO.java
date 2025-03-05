package com.banquito.paymentprocessor.procesarcores.banquito.controller.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para la transferencia de datos de transacción al core bancario")
public class TransaccionCoreDTO {
    
    @Schema(description = "Código único de la transacción", example = "TRX1234567", required = true)
    private String codTransaccion;
    
    @Schema(description = "Código único para identificar la transacción", example = "TRANS20240301123456", required = true)
    private String codigoUnico;
    
    @Schema(description = "Código del gateway que envía la transacción", example = "PAYPAL", required = true)
    private String codigoGtw;
    
    @Schema(description = "Número de la tarjeta", example = "4532123456789012", required = true)
    private String numeroTarjeta;
    
    @Schema(description = "Código de seguridad de la tarjeta", example = "123", required = true)
    private String cvv;
    
    @Schema(description = "Fecha de caducidad de la tarjeta", example = "12/25", required = true)
    private String fechaCaducidad;
    
    @Schema(description = "Monto de la transacción", example = "100.50", required = true)
    private BigDecimal monto;
    
    @Schema(description = "Código de moneda", example = "USD", required = false)
    private String codigoMoneda;
    
    @Schema(description = "Marca de la tarjeta", example = "VISA", required = false)
    private String marca;
    
    @Schema(description = "Estado de la transacción", example = "PEN", required = false)
    private String estado;
    
    @Schema(description = "Referencia de la transacción", example = "Compra en línea", required = false)
    private String referencia;
    
    @Schema(description = "País de origen de la transacción", example = "EC", required = false)
    private String pais;
    
    @Schema(description = "Tipo de transacción", example = "COM", required = true)
    private String tipo;
    
    @Schema(description = "Código SWIFT del banco del comercio", example = "BANKECXXXX", required = false)
    private String swiftBancoComercio;
    
    @Schema(description = "Cuenta IBAN del comercio", example = "EC1234567890123456789012", required = false)
    private String cuentaIbanComercio;
    
    @Schema(description = "Código SWIFT del banco emisor de la tarjeta", example = "BANKUS33XXX", required = false)
    private String swiftBancoTarjeta;
    
    @Schema(description = "Datos encriptados de la transacción", required = false)
    private String transaccionEncriptada;
    
    @Schema(description = "Número de meses para el diferido", example = "3", required = false)
    private Integer diferido;
    
    @Schema(description = "Número de cuotas para el diferido", example = "3", required = false)
    private Integer cuotas;
} 