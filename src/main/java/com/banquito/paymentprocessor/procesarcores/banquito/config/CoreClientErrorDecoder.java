package com.banquito.paymentprocessor.procesarcores.banquito.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.banquito.paymentprocessor.procesarcores.banquito.exception.CoreProcessingException;

import feign.Response;
import feign.codec.ErrorDecoder;

public class CoreClientErrorDecoder implements ErrorDecoder {
    
    private static final Logger log = LoggerFactory.getLogger(CoreClientErrorDecoder.class);
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() >= 400 && response.status() <= 499) {
            log.error("Error en la llamada al core bancario: {} - {}", response.status(), response.reason());
            return new CoreProcessingException(
                String.format("Error en la llamada al core bancario: %s", response.reason()),
                "ERROR-" + response.status()
            );
        }
        return defaultErrorDecoder.decode(methodKey, response);
    }
} 