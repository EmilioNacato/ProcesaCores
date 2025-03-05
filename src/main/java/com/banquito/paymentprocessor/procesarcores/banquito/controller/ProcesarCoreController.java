package com.banquito.paymentprocessor.procesarcores.banquito.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banquito.paymentprocessor.procesarcores.banquito.client.dto.ComercioRequestDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.client.dto.TarjetaRequestDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.controller.dto.TransaccionCoreDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.controller.dto.TransaccionCoreResponseDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.service.ProcesarCoreService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/core")
@Validated
@RequiredArgsConstructor
@Tag(name = "Core Transaccional", description = "API para el procesamiento de transacciones con el core bancario")
public class ProcesarCoreController {
    
    private static final Logger log = LoggerFactory.getLogger(ProcesarCoreController.class);
    
    private final ProcesarCoreService procesarCoreService;
    
    @PostMapping("/procesar-cores")
    @Operation(
        summary = "Procesa una transacción completa en el core bancario", 
        description = "Realiza el débito a la tarjeta y el crédito al comercio como una sola operación transaccional"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Transacción procesada correctamente",
            content = @Content(schema = @Schema(implementation = TransaccionCoreResponseDTO.class))
        ),
        @ApiResponse(responseCode = "400", description = "Datos de la solicitud inválidos"),
        @ApiResponse(responseCode = "422", description = "Error en el procesamiento de la transacción"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<TransaccionCoreResponseDTO> procesarTransaccion(@Valid @RequestBody TransaccionCoreDTO transaccion) {
        log.info("Recibida solicitud para procesar transacción: {}", transaccion.getCodigoUnico());
        
        TransaccionCoreResponseDTO respuesta = procesarCoreService.procesarTransaccion(transaccion);
        
        log.info("Transacción procesada - Estado: {}, Mensaje: {}", respuesta.getEstado(), respuesta.getMensaje());
        
        if ("APROBADO".equals(respuesta.getEstado())) {
            return ResponseEntity.ok(respuesta);
        } else {
            return ResponseEntity.unprocessableEntity().body(respuesta);
        }
    }
} 