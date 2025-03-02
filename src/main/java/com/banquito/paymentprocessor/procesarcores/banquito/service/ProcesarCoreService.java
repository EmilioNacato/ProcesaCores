package com.banquito.paymentprocessor.procesarcores.banquito.service;

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
import com.banquito.paymentprocessor.procesarcores.banquito.controller.dto.CoreResponse;
import com.banquito.paymentprocessor.procesarcores.banquito.controller.dto.TransaccionCoreDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.controller.dto.TransaccionCoreResponseDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.exception.CoreProcessingException;

import reactor.core.publisher.Mono;
import java.time.Duration;

@Service
public class ProcesarCoreService {
    
    private static final Logger log = LoggerFactory.getLogger(ProcesarCoreService.class);
    
    @Autowired
    private CoreBancarioClient coreBancarioClient;
    
    @Autowired
    private WebClient webClient;
    
    @Value("${core.timeout:30}")
    private int coreTimeout;
    
    @Value("${core.debit.url}")
    private String debitUrl;
    
    @Value("${core.credit.url}")
    private String creditUrl;
    
    /**
     * Procesa una transacción a través del core bancario.
     * Realiza dos operaciones secuenciales:
     * 1. Debitar la tarjeta del cliente
     * 2. Acreditar al comercio
     * 
     * Si cualquiera de estas operaciones falla, se revierte la operación de débito
     * para asegurar la integridad financiera.
     * 
     * @param transaccion Datos de la transacción a procesar
     * @return Respuesta del procesamiento con estado y mensaje
     */
    public TransaccionCoreResponseDTO procesarTransaccion(TransaccionCoreDTO transaccion) {
        log.info("Iniciando procesamiento de transacción en core bancario: {}", transaccion.getCodigoUnico());
        
        validarTransaccion(transaccion);
        
        TransaccionCoreResponseDTO response = new TransaccionCoreResponseDTO();
        response.setSwiftBanco(transaccion.getSwiftBanco());
        
        try {
            // Primera llamada: Débito de tarjeta
            log.info("Iniciando débito de tarjeta: {}", transaccion.getNumeroTarjeta());
            ResponseEntity<CoreResponse> debitResponse = procesarDebitoTarjeta(transaccion);
            
            if (debitResponse == null || debitResponse.getBody() == null) {
                log.error("Respuesta nula del servicio de débito");
                throw new CoreProcessingException("No se recibió respuesta del servicio de débito");
            }
            
            log.info("Respuesta de débito: {}", debitResponse.getBody());
            
            if (!isRespuestaExitosa(debitResponse)) {
                return crearRespuestaError("Error en débito de tarjeta: " + 
                    debitResponse.getBody().getMensaje(), "51");
            }

            // Segunda llamada: Crédito al comercio
            log.info("Iniciando crédito al comercio: {}", transaccion.getCodigoComercio());
            ResponseEntity<CoreResponse> creditResponse = procesarCreditoComercio(transaccion);
            
            if (creditResponse == null || creditResponse.getBody() == null) {
                log.error("Respuesta nula del servicio de crédito");
                // Revertimos el débito ya que el crédito falló
                revertirDebitoTarjeta(transaccion);
                throw new CoreProcessingException("No se recibió respuesta del servicio de crédito");
            }
            
            log.info("Respuesta de crédito: {}", creditResponse.getBody());
            
            if (!isRespuestaExitosa(creditResponse)) {
                // falla comercio, revertimos débito
                log.warn("Fallo en crédito al comercio, iniciando reversión del débito");
                revertirDebitoTarjeta(transaccion);
                return crearRespuestaError("Error en crédito al comercio: " + 
                    creditResponse.getBody().getMensaje(), "57");
            }

            log.info("Transacción procesada exitosamente: {}", transaccion.getCodigoUnico());
            response.setEstado("APROBADA");
            response.setCodigoRespuesta("00");
            response.setMensaje("Transacción procesada exitosamente");
            
        } catch (CoreProcessingException e) {
            log.error("Error específico del core: {}", e.getMessage());
            response.setEstado("ERROR");
            response.setCodigoRespuesta("96");
            response.setMensaje("Error en el core bancario: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al procesar transacción en core: {}", e.getMessage(), e);
            response.setEstado("ERROR");
            response.setCodigoRespuesta("96");
            response.setMensaje("Error en procesamiento: " + e.getMessage());
            // Intentamos revertir el débito si hubo un error general
            try {
                revertirDebitoTarjeta(transaccion);
            } catch (Exception ex) {
                log.error("Error al intentar revertir el débito: {}", ex.getMessage());
            }
        }
        
        return response;
    }

    /**
     * Valida que la transacción tenga todos los datos necesarios para ser procesada
     * 
     * @param transaccion Datos de la transacción a validar
     * @throws CoreProcessingException Si los datos no son válidos
     */
    private void validarTransaccion(TransaccionCoreDTO transaccion) {
        if (transaccion == null) {
            throw new CoreProcessingException("La transacción no puede ser nula");
        }
        
        if (transaccion.getNumeroTarjeta() == null || transaccion.getNumeroTarjeta().isEmpty()) {
            throw new CoreProcessingException("El número de tarjeta es requerido");
        }
        
        if (transaccion.getMonto() == null || transaccion.getMonto().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new CoreProcessingException("El monto debe ser mayor a cero");
        }
        
        if (transaccion.getCodigoUnico() == null || transaccion.getCodigoUnico().isEmpty()) {
            throw new CoreProcessingException("El código único de transacción es requerido");
        }
        
        log.debug("Transacción validada correctamente: {}", transaccion.getCodigoUnico());
    }

    /**
     * Procesa el débito en la tarjeta utilizando WebClient
     * 
     * @param transaccion Datos de la transacción
     * @return Respuesta del servicio de débito
     */
    private ResponseEntity<CoreResponse> procesarDebitoTarjeta(TransaccionCoreDTO transaccion) {
        log.debug("Enviando solicitud de débito al endpoint: {}", debitUrl);
        try {
            return webClient.post()
                .uri(debitUrl)
                .body(Mono.just(convertirATarjetaRequest(transaccion)), TarjetaRequestDTO.class)
                .retrieve()
                .toEntity(CoreResponse.class)
                .timeout(Duration.ofSeconds(coreTimeout))
                .block();
        } catch (Exception e) {
            log.error("Error al llamar al servicio de débito: {}", e.getMessage());
            throw new CoreProcessingException("Error en la comunicación con el servicio de débito: " + e.getMessage());
        }
    }

    /**
     * Procesa el crédito al comercio utilizando WebClient
     * 
     * @param transaccion Datos de la transacción
     * @return Respuesta del servicio de crédito
     */
    private ResponseEntity<CoreResponse> procesarCreditoComercio(TransaccionCoreDTO transaccion) {
        log.debug("Enviando solicitud de crédito al endpoint: {}", creditUrl);
        try {
            return webClient.post()
                .uri(creditUrl)
                .body(Mono.just(convertirAComercioRequest(transaccion)), ComercioRequestDTO.class)
                .retrieve()
                .toEntity(CoreResponse.class)
                .timeout(Duration.ofSeconds(coreTimeout))
                .block();
        } catch (Exception e) {
            log.error("Error al llamar al servicio de crédito: {}", e.getMessage());
            throw new CoreProcessingException("Error en la comunicación con el servicio de crédito: " + e.getMessage());
        }
    }

    /**
     * Revierte un débito de tarjeta en caso de fallo
     * 
     * @param transaccion Datos de la transacción a revertir
     */
    private void revertirDebitoTarjeta(TransaccionCoreDTO transaccion) {
        log.info("Iniciando reversión de débito para transacción: {}", transaccion.getCodigoUnico());
        try {
            ResponseEntity<CoreResponse> response = webClient.post()
                .uri(debitUrl + "/reversal")
                .body(Mono.just(convertirATarjetaRequest(transaccion)), TarjetaRequestDTO.class)
                .retrieve()
                .toEntity(CoreResponse.class)
                .timeout(Duration.ofSeconds(coreTimeout))
                .block();
                
            if (response != null && response.getBody() != null) {
                log.info("Reversión completada: {}", response.getBody().getMensaje());
            } else {
                log.warn("No se recibió respuesta clara de la reversión");
            }
        } catch (Exception e) {
            log.error("Error al revertir débito: {}", e.getMessage());
            // Aquí podríamos implementar un mecanismo de reintento o notificación
        }
    }

    /**
     * Verifica si la respuesta del core indica una transacción exitosa
     * 
     * @param response Respuesta del servicio del core
     * @return true si la transacción fue aprobada, false en caso contrario
     */
    private boolean isRespuestaExitosa(ResponseEntity<CoreResponse> response) {
        return response != null && 
               response.getBody() != null && 
               response.getBody().isAprobada();
    }

    /**
     * Crea una respuesta de error con el mensaje y código especificados
     * 
     * @param mensaje Mensaje de error
     * @param codigo Código de respuesta para el error
     * @return TransaccionCoreResponseDTO con los datos del error
     */
    private TransaccionCoreResponseDTO crearRespuestaError(String mensaje, String codigo) {
        TransaccionCoreResponseDTO response = new TransaccionCoreResponseDTO();
        response.setEstado("RECHAZADA");
        response.setCodigoRespuesta(codigo);
        response.setMensaje(mensaje);
        return response;
    }

    /**
     * Convierte un TransaccionCoreDTO a TarjetaRequestDTO para enviar al servicio de débito
     * 
     * @param transaccion TransaccionCoreDTO a convertir
     * @return TarjetaRequestDTO con los datos necesarios para el débito
     */
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

    /**
     * Convierte un TransaccionCoreDTO a ComercioRequestDTO para enviar al servicio de crédito
     * 
     * @param transaccion TransaccionCoreDTO a convertir
     * @return ComercioRequestDTO con los datos necesarios para el crédito
     */
    private ComercioRequestDTO convertirAComercioRequest(TransaccionCoreDTO transaccion) {
        ComercioRequestDTO request = new ComercioRequestDTO();
        request.setCodigoUnico(transaccion.getCodigoUnico());
        request.setMonto(transaccion.getMonto());
        request.setSwiftBancoComercio(transaccion.getSwiftBancoComercio());
        request.setCuentaIbanComercio(transaccion.getCuentaIbanComercio());
        request.setReferencia(transaccion.getReferencia());
        request.setTipo(transaccion.getTipo());
        request.setNombreComercio(transaccion.getCodigoComercio()); // Asumiendo que se usa el código como nombre
        request.setCodigoComercio(transaccion.getCodigoComercio());
        return request;
    }

    /**
     * Procesa solo el débito a la tarjeta en el core bancario
     * 
     * @param tarjeta Datos de la tarjeta para realizar el débito
     * @return Respuesta del procesamiento
     */
    public TransaccionCoreResponseDTO procesarDebitoTarjeta(TarjetaRequestDTO tarjeta) {
        log.info("Procesando débito a tarjeta para transacción: {}", tarjeta.getCodigoUnico());
        
        try {
            // Enviar petición de débito al core bancario
            ResponseEntity<CoreResponse> response = procesarDebitoTarjeta(convertirATarjetaDTO(tarjeta));
            
            if (!isRespuestaExitosa(response)) {
                String mensaje = response.getBody() != null ? response.getBody().getMensaje() : "Error desconocido";
                log.error("Error procesando débito a tarjeta: {}", mensaje);
                return crearRespuestaError(mensaje, response.getBody() != null ? 
                        response.getBody().getCodigoRespuesta() : "50");
            }
            
            // Creamos respuesta exitosa
            TransaccionCoreResponseDTO respuesta = new TransaccionCoreResponseDTO();
            respuesta.setEstado("APROBADA");
            respuesta.setCodigoRespuesta(response.getBody().getCodigoRespuesta());
            respuesta.setMensaje("Débito a tarjeta procesado exitosamente");
            respuesta.setSwiftBanco(response.getBody().getSwiftBanco());
            
            return respuesta;
        } catch (Exception e) {
            log.error("Error al procesar débito a tarjeta: {}", e.getMessage(), e);
            throw new CoreProcessingException("Error al procesar débito a tarjeta: " + e.getMessage(), e);
        }
    }

    /**
     * Procesa solo el crédito al comercio en el core bancario
     * 
     * @param comercio Datos del comercio para realizar el crédito
     * @return Respuesta del procesamiento
     */
    public TransaccionCoreResponseDTO procesarCreditoComercio(ComercioRequestDTO comercio) {
        log.info("Procesando crédito a comercio para transacción: {}", comercio.getCodigoUnico());
        
        try {
            // Enviar petición de crédito al comercio
            ResponseEntity<CoreResponse> response = procesarCreditoComercio(convertirAComercioDTO(comercio));
            
            if (!isRespuestaExitosa(response)) {
                String mensaje = response.getBody() != null ? response.getBody().getMensaje() : "Error desconocido";
                log.error("Error procesando crédito a comercio: {}", mensaje);
                return crearRespuestaError(mensaje, response.getBody() != null ? 
                        response.getBody().getCodigoRespuesta() : "60");
            }
            
            // Creamos respuesta exitosa
            TransaccionCoreResponseDTO respuesta = new TransaccionCoreResponseDTO();
            respuesta.setEstado("APROBADA");
            respuesta.setCodigoRespuesta(response.getBody().getCodigoRespuesta());
            respuesta.setMensaje("Crédito a comercio procesado exitosamente");
            respuesta.setSwiftBanco(response.getBody().getSwiftBanco());
            
            return respuesta;
        } catch (Exception e) {
            log.error("Error al procesar crédito a comercio: {}", e.getMessage(), e);
            throw new CoreProcessingException("Error al procesar crédito a comercio: " + e.getMessage(), e);
        }
    }

    /**
     * Convierte un DTO de tarjeta a un DTO de transacción
     */
    private TransaccionCoreDTO convertirATarjetaDTO(TarjetaRequestDTO tarjeta) {
        TransaccionCoreDTO transaccion = new TransaccionCoreDTO();
        transaccion.setNumeroTarjeta(tarjeta.getNumeroTarjeta());
        transaccion.setCvv(tarjeta.getCvv());
        transaccion.setFechaCaducidad(tarjeta.getFechaCaducidad());
        transaccion.setMonto(tarjeta.getMonto());
        transaccion.setCodigoUnico(tarjeta.getCodigoUnico());
        transaccion.setTipo(tarjeta.getTipo());
        transaccion.setSwiftBanco(tarjeta.getSwiftBanco());
        transaccion.setReferencia(tarjeta.getReferencia());
        transaccion.setCodigoGateway(tarjeta.getCodigoGateway());
        transaccion.setMesesDiferido(tarjeta.getMesesDiferido());
        transaccion.setTipoDiferido(tarjeta.getTipoDiferido());
        return transaccion;
    }

    /**
     * Convierte un DTO de comercio a un DTO de transacción
     */
    private TransaccionCoreDTO convertirAComercioDTO(ComercioRequestDTO comercio) {
        TransaccionCoreDTO transaccion = new TransaccionCoreDTO();
        transaccion.setCodigoUnico(comercio.getCodigoUnico());
        transaccion.setMonto(comercio.getMonto());
        transaccion.setSwiftBancoComercio(comercio.getSwiftBancoComercio());
        transaccion.setCuentaIbanComercio(comercio.getCuentaIbanComercio());
        transaccion.setReferencia(comercio.getReferencia());
        transaccion.setTipo(comercio.getTipo());
        transaccion.setCodigoComercio(comercio.getCodigoComercio());
        return transaccion;
    }
}

