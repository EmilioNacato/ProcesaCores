package com.banquito.paymentprocessor.procesarcores.banquito.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.banquito.paymentprocessor.procesarcores.banquito.client.CoreBancarioClient;
import com.banquito.paymentprocessor.procesarcores.banquito.controller.dto.TransaccionCoreDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.exception.CoreProcessingException;

@Service
public class ProcesarCoreService {
    
    private static final Logger log = LoggerFactory.getLogger(ProcesarCoreService.class);
    private final CoreBancarioClient coreBancarioClient;

    public ProcesarCoreService(CoreBancarioClient coreBancarioClient) {
        this.coreBancarioClient = coreBancarioClient;
    }

    public void procesarTransaccion(TransaccionCoreDTO transaccion) {
        try {
            log.info("Iniciando procesamiento de transacción en el core para la referencia: {}", 
                transaccion.getReferencia());

            this.coreBancarioClient.procesarTransaccion(transaccion);
            
            log.info("Transacción procesada exitosamente en el core para la referencia: {}", 
                transaccion.getReferencia());
        } catch (Exception e) {
            log.error("Error al procesar la transacción en el core: {}", e.getMessage());
            throw new CoreProcessingException("Error al procesar la transacción en el core", 
                transaccion.getReferencia());
        }
    }
} 