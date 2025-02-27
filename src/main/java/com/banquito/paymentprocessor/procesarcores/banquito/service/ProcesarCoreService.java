package com.banquito.paymentprocessor.procesarcores.banquito.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import com.banquito.paymentprocessor.procesarcores.banquito.client.CoreBancarioClient;
import com.banquito.paymentprocessor.procesarcores.banquito.client.dto.TarjetaRequestDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.client.dto.ComercioRequestDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.controller.dto.TransaccionCoreDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.controller.dto.CoreResponse;
import com.banquito.paymentprocessor.procesarcores.banquito.controller.dto.TransaccionCoreResponseDTO;

import reactor.core.publisher.Mono;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ProcesarCoreService {

    private static final Logger log = LoggerFactory.getLogger(ProcesarCoreService.class);
    private final CoreBancarioClient coreBancarioClient;
    private final WebClient webClient;

    @Value("${core.timeout:30}")
    private Integer coreTimeout;

    @Value("${core.debit.url}")
    private String debitUrl;

    @Value("${core.credit.url}")
    private String creditUrl;

    public ProcesarCoreService(CoreBancarioClient coreBancarioClient, WebClient.Builder webClientBuilder) {
        this.coreBancarioClient = coreBancarioClient;
        this.webClient = webClientBuilder.build();
    }

    public TransaccionCoreResponseDTO procesarTransaccion(TransaccionCoreDTO transaccion) {
        TransaccionCoreResponseDTO response = new TransaccionCoreResponseDTO();
        
        try {
            // Primera llamada: Débito de la tarjeta
            log.info("Iniciando débito de tarjeta para transacción: {}", transaccion.getNumeroTarjeta());
            ResponseEntity<CoreResponse> debitResponse = procesarDebitoTarjeta(transaccion);
            
            if (!isRespuestaExitosa(debitResponse)) {
                return crearRespuestaError("Error en débito de tarjeta: " + 
                    debitResponse.getBody().getMensaje());
            }

            // Segunda llamada: Crédito al comercio
            log.info("Iniciando crédito al comercio: {}", transaccion.getCodigoComercio());
            ResponseEntity<CoreResponse> creditResponse = procesarCreditoComercio(transaccion);
            
            if (!isRespuestaExitosa(creditResponse)) {
                // falla comercio, revertimos débito
                revertirDebitoTarjeta(transaccion);
                return crearRespuestaError("Error en crédito al comercio: " + 
                    creditResponse.getBody().getMensaje());
            }

            response.setEstado("APROBADA");
            response.setCodigoRespuesta("00");
            response.setMensaje("Transacción procesada exitosamente");
            
        } catch (Exception e) {
            log.error("Error al procesar transacción en core: {}", e.getMessage());
            response.setEstado("ERROR");
            response.setCodigoRespuesta("96");
            response.setMensaje("Error en procesamiento: " + e.getMessage());
        }
        
        return response;
    }

    private ResponseEntity<CoreResponse> procesarDebitoTarjeta(TransaccionCoreDTO transaccion) {
        return webClient.post()
            .uri(debitUrl)
            .body(Mono.just(transaccion), TransaccionCoreDTO.class)
            .retrieve()
            .toEntity(CoreResponse.class)
            .timeout(Duration.ofSeconds(coreTimeout))
            .block();
    }

    private ResponseEntity<CoreResponse> procesarCreditoComercio(TransaccionCoreDTO transaccion) {
        return webClient.post()
            .uri(creditUrl)
            .body(Mono.just(transaccion), TransaccionCoreDTO.class)
            .retrieve()
            .toEntity(CoreResponse.class)
            .timeout(Duration.ofSeconds(coreTimeout))
            .block();
    }

    private void revertirDebitoTarjeta(TransaccionCoreDTO transaccion) {
        log.info("Iniciando reversión de débito para transacción: {}", transaccion.getNumeroTarjeta());
        try {
            webClient.post()
                .uri(debitUrl + "/reversal")
                .body(Mono.just(transaccion), TransaccionCoreDTO.class)
                .retrieve()
                .toEntity(CoreResponse.class)
                .timeout(Duration.ofSeconds(coreTimeout))
                .block();
        } catch (Exception e) {
            log.error("Error al revertir débito: {}", e.getMessage());
        }
    }

    private boolean isRespuestaExitosa(ResponseEntity<CoreResponse> response) {
        return response != null && 
               response.getBody() != null && 
               response.getBody().isAprobada();
    }

    private TransaccionCoreResponseDTO crearRespuestaError(String mensaje) {
        TransaccionCoreResponseDTO response = new TransaccionCoreResponseDTO();
        response.setEstado("RECHAZADA");
        response.setCodigoRespuesta("51");
        response.setMensaje(mensaje);
        return response;
    }

    private TarjetaRequestDTO convertirATarjetaRequest(TransaccionCoreDTO transaccion) {
        TarjetaRequestDTO request = new TarjetaRequestDTO();
        request.setNumeroTarjeta(transaccion.getNumeroTarjeta());
        request.setCvv(transaccion.getCvv());
        request.setFechaCaducidad(transaccion.getFechaCaducidad());
        request.setMonto(transaccion.getMonto());
        request.setCodigoUnico(transaccion.getCodigoUnico());
        request.setTipo(transaccion.getTipo());
        request.setSwiftBanco(transaccion.getSwiftBanco());
        request.setReferencia(transaccion.getReferencia());
        request.setCodigoGateway(transaccion.getCodigoGateway());
        request.setMesesDiferido(transaccion.getMesesDiferido());
        request.setTipoDiferido(transaccion.getTipoDiferido());
        return request;
    }

    private ComercioRequestDTO convertirAComercioRequest(TransaccionCoreDTO transaccion) {
        ComercioRequestDTO request = new ComercioRequestDTO();
        request.setCodigoUnico(transaccion.getCodigoUnico());
        request.setMonto(transaccion.getMonto());
        request.setSwiftBancoComercio(transaccion.getSwiftBancoComercio());
        request.setCuentaIbanComercio(transaccion.getCuentaIbanComercio());
        request.setReferencia(transaccion.getReferencia());
        request.setTipo(transaccion.getTipo());
        return request;
    }
}

