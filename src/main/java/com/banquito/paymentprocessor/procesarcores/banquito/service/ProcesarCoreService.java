package com.banquito.paymentprocessor.procesarcores.banquito.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.banquito.paymentprocessor.procesarcores.banquito.client.CoreBancarioClient;
import com.banquito.paymentprocessor.procesarcores.banquito.client.dto.ComercioRequestDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.client.dto.CoreResponseDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.client.dto.TarjetaRequestDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.controller.dto.TransaccionCoreDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.controller.dto.TransaccionCoreResponseDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.exception.CoreProcessingException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProcesarCoreService {
    
    private static final Logger log = LoggerFactory.getLogger(ProcesarCoreService.class);
    
    private final CoreBancarioClient coreBancarioClient;
    
    @Autowired
    private WebClient webClient;
    
    @Value("${core.timeout:30}")
    private int coreTimeout;
    
    @Value("${core.debit.url}")
    private String debitUrl;
    
    @Value("${core.credit.url}")
    private String creditUrl;
    
    public TransaccionCoreResponseDTO procesarTransaccion(TransaccionCoreDTO transaccion) {
        log.info("Procesando transacción completa: {}", transaccion.getCodigoUnico());
        
        try {
            // Paso 1: Procesar transacción tarjeta (primer requisito)
            ResponseEntity<CoreResponseDTO> respuestaTarjeta = procesarTransaccionTarjeta(transaccion);
            validarRespuestaCore(respuestaTarjeta, "tarjeta");
            
            log.info("Transacción a tarjeta completada correctamente: {}", transaccion.getCodigoUnico());
            
            // Paso 2: Procesar transacción cuenta comercio (segundo requisito)
            ResponseEntity<CoreResponseDTO> respuestaCuenta = procesarTransaccionCuenta(transaccion);
            validarRespuestaCore(respuestaCuenta, "cuenta");
            
            log.info("Transacción a cuenta de comercio completada correctamente: {}", transaccion.getCodigoUnico());
            
            // Si ambos pasos fueron exitosos, retornar respuesta exitosa
            return TransaccionCoreResponseDTO.builder()
                    .codigoUnico(transaccion.getCodigoUnico())
                    .fechaProceso(LocalDateTime.now())
                    .estado("APROBADO")
                    .mensaje("Transacción procesada correctamente")
                    .build();
            
        } catch (Exception e) {
            log.error("Error al procesar transacción: {}", e.getMessage(), e);
            return TransaccionCoreResponseDTO.builder()
                    .codigoUnico(transaccion.getCodigoUnico())
                    .fechaProceso(LocalDateTime.now())
                    .estado("RECHAZADO")
                    .mensaje("Error en procesamiento: " + e.getMessage())
                    .build();
        }
    }
    
    private ResponseEntity<CoreResponseDTO> procesarTransaccionTarjeta(TransaccionCoreDTO transaccion) {
        log.info("Iniciando procesamiento de débito a tarjeta: {}", transaccion.getCodigoUnico());
        
        try {
            TarjetaRequestDTO request = convertirATarjetaRequest(transaccion);
            log.debug("Request para débito a tarjeta: {}", request);
            
            ResponseEntity<CoreResponseDTO> respuesta = coreBancarioClient.procesarTransaccionTarjeta(request);
            log.info("Respuesta del core para débito a tarjeta: {}", respuesta.getStatusCode());
            
            return respuesta;
        } catch (Exception e) {
            log.error("Error al procesar débito a tarjeta: {}", e.getMessage());
            throw new CoreProcessingException("Error al procesar débito a tarjeta: " + e.getMessage());
        }
    }
    
    private ResponseEntity<CoreResponseDTO> procesarTransaccionCuenta(TransaccionCoreDTO transaccion) {
        log.info("Iniciando procesamiento de crédito a cuenta de comercio: {}", transaccion.getCodigoUnico());
        
        try {
            ComercioRequestDTO request = convertirAComercioRequest(transaccion);
            log.debug("Request para crédito a cuenta de comercio: {}", request);
            
            ResponseEntity<CoreResponseDTO> respuesta = coreBancarioClient.procesarTransaccionCuenta(request);
            log.info("Respuesta del core para crédito a cuenta de comercio: {}", respuesta.getStatusCode());
            
            return respuesta;
        } catch (Exception e) {
            log.error("Error al procesar crédito a cuenta de comercio: {}", e.getMessage());
            throw new CoreProcessingException("Error al procesar crédito a cuenta de comercio: " + e.getMessage());
        }
    }
    
    private void validarRespuestaCore(ResponseEntity<CoreResponseDTO> respuesta, String tipo) {
        if (respuesta == null || respuesta.getBody() == null) {
            throw new CoreProcessingException("No se recibió respuesta del core bancario para " + tipo);
        }
        
        CoreResponseDTO coreResponse = respuesta.getBody();
        
        if (!"APROBADO".equals(coreResponse.getEstado())) {
            throw new CoreProcessingException("Transacción rechazada por el core para " + tipo + ": " 
                    + coreResponse.getMensaje());
        }
    }
    
    private TarjetaRequestDTO convertirATarjetaRequest(TransaccionCoreDTO transaccion) {
        log.debug("Convirtiendo transacción a TarjetaRequestDTO: {}", transaccion.getCodigoUnico());
        
        // Asignar valores por defecto para campos obligatorios si es necesario
        String tipo = (transaccion.getTipo() != null) ? transaccion.getTipo() : "COM";
        String moneda = (transaccion.getCodigoMoneda() != null) ? transaccion.getCodigoMoneda() : "USD";
        String pais = (transaccion.getPais() != null) ? transaccion.getPais() : "EC";
        
        return TarjetaRequestDTO.builder()
                .tipo(tipo)
                .monto(transaccion.getMonto())
                .moneda(moneda)
                .pais(pais)
                .swift(transaccion.getSwiftBancoTarjeta())
                .numeroTarjeta(transaccion.getNumeroTarjeta())
                .codigoUnicoTransaccion(transaccion.getCodigoUnico())
                .referencia(transaccion.getReferencia())
                .transaccionEncriptada(transaccion.getTransaccionEncriptada())
                .diferido(transaccion.getDiferido() != null && transaccion.getDiferido() > 0)
                .cuotas(transaccion.getCuotas() != null ? transaccion.getCuotas() : 1)
                .build();
    }
    
    private ComercioRequestDTO convertirAComercioRequest(TransaccionCoreDTO transaccion) {
        log.debug("Convirtiendo transacción a ComercioRequestDTO: {}", transaccion.getCodigoUnico());
        
        // Asignar valores por defecto para campos obligatorios si es necesario
        String tipo = (transaccion.getTipo() != null) ? transaccion.getTipo() : "COM";
        
        return ComercioRequestDTO.builder()
                .iban(transaccion.getCuentaIbanComercio())
                .swift(transaccion.getSwiftBancoComercio())
                .tipo(tipo)
                .codigoUnico(transaccion.getCodigoUnico())
                .monto(transaccion.getMonto())
                .referencia(transaccion.getReferencia())
                .build();
    }
}

