package com.banquito.paymentprocessor.procesarcores.banquito.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "DTO para procesar una transacción a través del core bancario")
public class TransaccionCoreDTO {

    @NotBlank(message = "El número de tarjeta es obligatorio")
    @Size(min = 15, max = 16, message = "El número de tarjeta debe tener entre 15 y 16 dígitos")
    @Pattern(regexp = "^[0-9]+$", message = "El número de tarjeta debe contener solo dígitos")
    @Schema(description = "Número de la tarjeta a debitar", example = "4557880123456789", required = true)
    private String numeroTarjeta;
    
    @NotBlank(message = "El CVV es obligatorio")
    @Size(min = 3, max = 4, message = "El CVV debe tener entre 3 y 4 dígitos")
    @Pattern(regexp = "^[0-9]+$", message = "El CVV debe contener solo dígitos")
    @Schema(description = "Código de seguridad de la tarjeta", example = "123", required = true)
    private String cvv;
    
    @NotNull(message = "La fecha de caducidad es obligatoria")
    @Future(message = "La fecha de caducidad debe ser una fecha futura")
    @Schema(description = "Fecha de caducidad de la tarjeta", required = true)
    private LocalDateTime fechaCaducidad;
    
    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    @Digits(integer = 12, fraction = 2, message = "El monto debe tener como máximo 12 dígitos enteros y 2 decimales")
    @Schema(description = "Monto a procesar", example = "100.50", required = true)
    private BigDecimal monto;
    
    @NotBlank(message = "El código único es obligatorio")
    @Size(max = 50, message = "El código único debe tener máximo 50 caracteres")
    @Schema(description = "Código único para identificar la transacción", example = "TX123456789", required = true)
    private String codigoUnico;
    
    @NotBlank(message = "El tipo de transacción es obligatorio")
    @Schema(description = "Tipo de transacción (COMPRA, RETIRO, etc)", example = "COMPRA", required = true)
    private String tipo;
    
    @NotBlank(message = "El código swift del banco es obligatorio")
    @Schema(description = "Código SWIFT del banco emisor de la tarjeta", example = "BANECUXXXX", required = true)
    private String swiftBanco;
    
    @NotBlank(message = "El código swift del banco del comercio es obligatorio")
    @Schema(description = "Código SWIFT del banco del comercio", example = "PICHEQXX", required = true)
    private String swiftBancoComercio;
    
    @NotBlank(message = "La cuenta IBAN del comercio es obligatoria")
    @Schema(description = "Cuenta IBAN del comercio donde se acreditará el monto", 
           example = "EC11 0000 0000 0123 4567 8901", required = true)
    private String cuentaIbanComercio;
    
    @Schema(description = "Referencia de la transacción", example = "Compra en línea")
    private String referencia;
    
    @Schema(description = "Código asignado por el gateway de pago", example = "GW987654321")
    private String codigoGateway;
    
    @Schema(description = "Meses del diferido (1-48)", example = "3")
    private Integer mesesDiferido;
    
    @Schema(description = "Tipo de diferido (NORMAL, SIN_INTERESES)", example = "NORMAL")
    private String tipoDiferido;
    
    @NotBlank(message = "El código del comercio es obligatorio")
    @Schema(description = "Código del comercio", example = "COM123456", required = true)
    private String codigoComercio;
} 