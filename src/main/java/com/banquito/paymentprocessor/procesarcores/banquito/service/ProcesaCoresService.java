package com.banquito.paymentprocessor.procesarcores.banquito.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import java.time.Duration;

@Service
@Slf4j
public class ProcesaCoresService {
    
    @Value("${core.timeout}")
    private Integer coreTimeout;

    private final WebClient webClient;

    public ProcesaCoresService(WebClient webClient) {
        this.webClient = webClient;
    }

    public TransaccionCoreResponseDTO procesarTransaccion(TransaccionCoreDTO transaccion) {
        TransaccionCoreResponseDTO response = new TransaccionCoreResponseDTO();
        
        try {
            ResponseEntity<CoreResponse> validacionTarjeta = procesarValidacionTarjeta(transaccion);
            
            if (validacionTarjeta.getStatusCode() == HttpStatus.OK && 
                validacionTarjeta.getBody().isAprobada()) {
                
                ResponseEntity<CoreResponse> acreditacionComercio = procesarAcreditacionComercio(transaccion);
                
                if (acreditacionComercio.getStatusCode() == HttpStatus.OK && 
                    acreditacionComercio.getBody().isAprobada()) {
                    response.setEstado("APROBADA");
                    response.setCodigoRespuesta("00");
                } else {
                    response.setEstado("RECHAZADA");
                    response.setCodigoRespuesta("51");
                    response.setMensaje("Error en acreditación al comercio");
                }
            } else {
                response.setEstado("RECHAZADA");
                response.setCodigoRespuesta("05");
                response.setMensaje("Tarjeta rechazada");
            }
        } catch (Exception e) {
            log.error("Error al procesar transacción en core: {}", e.getMessage());
            response.setEstado("ERROR");
            response.setCodigoRespuesta("96");
            response.setMensaje("Error en procesamiento");
        }
        
        return response;
    }

    private ResponseEntity<CoreResponse> procesarValidacionTarjeta(TransaccionCoreDTO transaccion) {
        return webClient.post()
            .uri("/api/core/validar-tarjeta")
            .body(Mono.just(transaccion), TransaccionCoreDTO.class)
            .retrieve()
            .toEntity(CoreResponse.class)
            .timeout(Duration.ofSeconds(coreTimeout))
            .block();
    }

    private ResponseEntity<CoreResponse> procesarAcreditacionComercio(TransaccionCoreDTO transaccion) {
        return webClient.post()
            .uri("/api/core/acreditar-comercio")
            .body(Mono.just(transaccion), TransaccionCoreDTO.class)
            .retrieve()
            .toEntity(CoreResponse.class)
            .timeout(Duration.ofSeconds(coreTimeout))
            .block();
    }
} 