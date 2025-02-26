package com.banquito.paymentprocessor.procesarcores.banquito.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import com.banquito.paymentprocessor.procesarcores.banquito.client.CoreBancarioClient;
import com.banquito.paymentprocessor.procesarcores.banquito.client.dto.TarjetaRequestDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.client.dto.ComercioRequestDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.client.dto.CoreResponseDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.controller.dto.TransaccionCoreDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.exception.CoreProcessingException;

@Service
public class ProcesarCoreService {

    private static final Logger log = LoggerFactory.getLogger(ProcesarCoreService.class);
    private final CoreBancarioClient coreBancarioClient;

    @Value("${core.timeout}")
    private Integer coreTimeout;

    public ProcesarCoreService(CoreBancarioClient coreBancarioClient) {
        this.coreBancarioClient = coreBancarioClient;
    }

    public void procesarTransaccion(TransaccionCoreDTO transaccion) {
        try {
            log.info("Iniciando procesamiento de transacción en el core para la referencia: {}", transaccion.getReferencia());

            // Validación de la tarjeta
            log.info("Procesando validación de tarjeta");
            TarjetaRequestDTO tarjetaRequest = convertirATarjetaRequest(transaccion);
            CoreResponseDTO respuestaTarjeta = coreBancarioClient.procesarTransaccionTarjeta(tarjetaRequest);

            if (!respuestaTarjeta.getTransaccionExitosa()) {
                log.error("Error en la validación de la tarjeta: {}", respuestaTarjeta.getMensaje());
                throw new CoreProcessingException("Error en la validación de la tarjeta: " + respuestaTarjeta.getMensaje(), transaccion.getReferencia());
            }

            log.info("Tarjeta validada exitosamente");

            // ✅ Acreditación al comercio
            log.info("Procesando acreditación al comercio");
            ComercioRequestDTO comercioRequest = convertirAComercioRequest(transaccion);
            CoreResponseDTO respuestaComercio = coreBancarioClient.procesarTransaccionComercio(comercioRequest);

            if (!respuestaComercio.getTransaccionExitosa()) {
                log.error("Error en la acreditación al comercio: {}", respuestaComercio.getMensaje());
                throw new CoreProcessingException("Error en la acreditación al comercio: " + respuestaComercio.getMensaje(), transaccion.getReferencia());
            }

            log.info("Acreditación al comercio completada exitosamente");

            log.info("Transacción procesada exitosamente en el core para la referencia: {}", transaccion.getReferencia());
        } catch (Exception e) {
            log.error("Error al procesar la transacción en el core: {}", e.getMessage());
            throw new CoreProcessingException("Error al procesar la transacción en el core: " + e.getMessage(), transaccion.getReferencia());
        }
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
