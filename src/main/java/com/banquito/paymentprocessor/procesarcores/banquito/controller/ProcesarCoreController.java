package com.banquito.paymentprocessor.procesarcores.banquito.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banquito.paymentprocessor.procesarcores.banquito.client.dto.ComercioRequestDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.client.dto.TarjetaRequestDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.controller.dto.TransaccionCoreDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.controller.dto.TransaccionCoreResponseDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.exception.CoreProcessingException;
import com.banquito.paymentprocessor.procesarcores.banquito.service.ProcesarCoreService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/core")
@Tag(name = "Core Bancario", description = "API para procesar transacciones en el core bancario")
public class ProcesarCoreController {
    
    private static final Logger log = LoggerFactory.getLogger(ProcesarCoreController.class);
    
    private final ProcesarCoreService procesarCoreService;
    
    public ProcesarCoreController(ProcesarCoreService procesarCoreService) {
        this.procesarCoreService = procesarCoreService;
    }
    
    @PostMapping("/procesar")
    @Operation(
        summary = "Procesa una transacción completa en el core bancario", 
        description = "Recibe los datos de una transacción para procesarla en el core bancario. " +
                     "El proceso incluye débito a la tarjeta y crédito al comercio."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Transacción procesada (aprobada o rechazada)",
            content = @Content(schema = @Schema(implementation = TransaccionCoreResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Petición inválida, faltan campos obligatorios o datos incorrectos"
        ),
        @ApiResponse(
            responseCode = "422", 
            description = "No se pudo procesar la transacción por errores en los datos"
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Error interno en el servidor"
        )
    })
    public ResponseEntity<TransaccionCoreResponseDTO> procesarTransaccion(
            @Parameter(
                description = "Datos de la transacción a procesar", 
                required = true
            ) 
            @Valid @RequestBody TransaccionCoreDTO transaccion) {
        
        log.info("Recibida solicitud para procesar transacción: {}", transaccion.getCodigoUnico());
        
        try {
            // Validación adicional de datos de negocio
            if (transaccion.getMonto() == null || transaccion.getMonto().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                log.warn("Monto no válido: {}", transaccion.getMonto());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(crearRespuestaError("El monto debe ser mayor a cero", "13"));
            }
            
            // Proceso principal
            TransaccionCoreResponseDTO respuesta = procesarCoreService.procesarTransaccion(transaccion);
            
            // Determinamos el estado HTTP según la respuesta
            if ("ERROR".equals(respuesta.getEstado())) {
                log.error("Error en procesamiento: {}", respuesta.getMensaje());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
            } else if ("RECHAZADA".equals(respuesta.getEstado())) {
                log.warn("Transacción rechazada: {}", respuesta.getMensaje());
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(respuesta);
            } else {
                log.info("Transacción procesada exitosamente: {}", respuesta.getCodigoRespuesta());
                return ResponseEntity.ok(respuesta);
            }
            
        } catch (CoreProcessingException e) {
            log.error("Error específico del core: {}", e.getMessage());
            TransaccionCoreResponseDTO errorResponse = crearRespuestaError(e.getMessage(), e.getErrorCode() != null ? e.getErrorCode() : "96");
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
        } catch (Exception e) {
            log.error("Error inesperado al procesar transacción: {}", e.getMessage(), e);
            TransaccionCoreResponseDTO errorResponse = crearRespuestaError(
                    "Error interno del servidor: " + e.getMessage(), "99");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping("/debito-tarjeta")
    @Operation(
        summary = "Procesa el débito a la tarjeta en el core bancario", 
        description = "Ejecuta solo la parte de débito a la tarjeta del proceso de transacción."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Débito procesado exitosamente",
            content = @Content(schema = @Schema(implementation = TransaccionCoreResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Petición inválida"
        ),
        @ApiResponse(
            responseCode = "422", 
            description = "No se pudo procesar el débito"
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Error interno en el servidor"
        )
    })
    public ResponseEntity<TransaccionCoreResponseDTO> procesarDebitoTarjeta(
            @Parameter(description = "Datos de la tarjeta para debitar", required = true) 
            @Valid @RequestBody TarjetaRequestDTO tarjeta) {
        
        log.info("Procesando débito a tarjeta para transacción: {}", tarjeta.getCodigoUnico());
        try {
            TransaccionCoreResponseDTO respuesta = procesarCoreService.procesarDebitoTarjeta(tarjeta);
            return determinarRespuestaHttp(respuesta);
        } catch (Exception e) {
            log.error("Error al procesar débito: {}", e.getMessage());
            TransaccionCoreResponseDTO errorResponse = crearRespuestaError(
                    "Error al procesar débito: " + e.getMessage(), "97");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping("/credito-comercio")
    @Operation(
        summary = "Procesa el crédito al comercio en el core bancario", 
        description = "Ejecuta solo la parte de crédito al comercio del proceso de transacción."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Crédito procesado exitosamente",
            content = @Content(schema = @Schema(implementation = TransaccionCoreResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Petición inválida"
        ),
        @ApiResponse(
            responseCode = "422", 
            description = "No se pudo procesar el crédito"
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Error interno en el servidor"
        )
    })
    public ResponseEntity<TransaccionCoreResponseDTO> procesarCreditoComercio(
            @Parameter(description = "Datos del comercio para acreditar", required = true) 
            @Valid @RequestBody ComercioRequestDTO comercio) {
        
        log.info("Procesando crédito a comercio para transacción: {}", comercio.getCodigoUnico());
        try {
            TransaccionCoreResponseDTO respuesta = procesarCoreService.procesarCreditoComercio(comercio);
            return determinarRespuestaHttp(respuesta);
        } catch (Exception e) {
            log.error("Error al procesar crédito: {}", e.getMessage());
            TransaccionCoreResponseDTO errorResponse = crearRespuestaError(
                    "Error al procesar crédito: " + e.getMessage(), "98");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    private ResponseEntity<TransaccionCoreResponseDTO> determinarRespuestaHttp(TransaccionCoreResponseDTO respuesta) {
        if ("ERROR".equals(respuesta.getEstado())) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
        } else if ("RECHAZADA".equals(respuesta.getEstado())) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(respuesta);
        } else {
            return ResponseEntity.ok(respuesta);
        }
    }
    
    private TransaccionCoreResponseDTO crearRespuestaError(String mensaje, String codigo) {
        TransaccionCoreResponseDTO respuesta = new TransaccionCoreResponseDTO();
        respuesta.setEstado("ERROR");
        respuesta.setCodigoRespuesta(codigo);
        respuesta.setMensaje(mensaje);
        return respuesta;
    }
} 