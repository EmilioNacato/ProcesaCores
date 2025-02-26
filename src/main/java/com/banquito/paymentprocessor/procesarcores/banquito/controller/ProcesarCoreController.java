package com.banquito.paymentprocessor.procesarcores.banquito.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banquito.paymentprocessor.procesarcores.banquito.dto.TransaccionCoreDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.dto.TransaccionCoreResponseDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.exception.CoreProcessingException;
import com.banquito.paymentprocessor.procesarcores.banquito.service.ProcesarCoreService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/core")
@Tag(name = "Procesador de Cores", description = "API para procesar transacciones en los cores bancarios")
public class ProcesarCoreController {

    private static final Logger log = LoggerFactory.getLogger(ProcesarCoreController.class);
    private final ProcesarCoreService service;

    public ProcesarCoreController(ProcesarCoreService service) {
        this.service = service;
    }

    @PostMapping("/procesar")
    @Operation(summary = "Procesa una transacción en el core bancario", 
              description = "Procesa la transacción tanto para la tarjeta como para el comercio. " +
                          "La transacción solo se considera exitosa si ambos procesos son completados.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transacción procesada exitosamente en ambos cores"),
        @ApiResponse(responseCode = "400", description = "Datos de la transacción inválidos"),
        @ApiResponse(responseCode = "422", description = "Error en el procesamiento de la transacción"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<Void> procesarTransaccion(@Valid @RequestBody TransaccionCoreDTO transaccion) {
        try {
            log.info("Recibida solicitud para procesar transacción con referencia: {}", 
                transaccion.getReferencia());
            
            this.service.procesarTransaccion(transaccion);
            
            log.info("Transacción procesada exitosamente con referencia: {}", 
                transaccion.getReferencia());
            
            return ResponseEntity.ok().build();
        } catch (CoreProcessingException e) {
            log.error("Error al procesar la transacción: {}", e.getMessage());
            return ResponseEntity.unprocessableEntity().build();
        } catch (Exception e) {
            log.error("Error interno al procesar la transacción: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
} 