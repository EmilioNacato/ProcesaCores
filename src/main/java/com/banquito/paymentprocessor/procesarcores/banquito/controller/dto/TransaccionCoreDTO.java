package com.banquito.paymentprocessor.procesarcores.banquito.controller.dto;

import java.math.BigDecimal;
import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "DTO para procesar transacciones en el core bancario")
public class TransaccionCoreDTO {
    
    @NotBlank(message = "El número de tarjeta es requerido")
    @Size(min = 16, max = 16, message = "El número de tarjeta debe tener 16 dígitos")
    private String numeroTarjeta;

    @NotNull(message = "La fecha de caducidad es requerida")
    private Date fechaCaducidad;

    @NotNull(message = "El monto es requerido")
    private BigDecimal monto;

    @NotBlank(message = "El código de moneda es requerido")
    @Size(min = 3, max = 3, message = "El código de moneda debe tener 3 caracteres")
    private String codigoMoneda;

    @NotBlank(message = "La marca es requerida")
    @Pattern(regexp = "VISA|MASTERCARD", message = "La marca debe ser VISA o MASTERCARD")
    private String marca;

    @NotBlank(message = "El SWIFT del banco es requerido")
    @Size(min = 8, max = 11, message = "El SWIFT del banco debe tener entre 8 y 11 caracteres")
    private String swiftBancoTarjeta;

    @NotBlank(message = "El SWIFT del banco de comercio es requerido")
    @Size(min = 8, max = 11, message = "El SWIFT del banco de comercio debe tener entre 8 y 11 caracteres")
    private String swiftBancoComercio;

    @NotBlank(message = "La cuenta IBAN del comercio es requerida")
    @Size(min = 20, max = 28, message = "La cuenta IBAN debe tener entre 20 y 28 caracteres")
    private String cuentaIbanComercio;

    @NotBlank(message = "La referencia es requerida")
    @Size(max = 50, message = "La referencia no puede exceder los 50 caracteres")
    private String referencia;
} 