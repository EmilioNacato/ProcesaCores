package com.banquito.paymentprocessor.procesarcores.banquito.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import feign.RetryableException;
import feign.Retryer;

public class CoreClientRetryer implements Retryer {
    
    private static final Logger log = LoggerFactory.getLogger(CoreClientRetryer.class);
    
    @Value("${core.bancario.retry.max-attempts}")
    private int maxAttempts;
    
    @Value("${core.bancario.retry.backoff}")
    private long backoff;
    
    private int attempt = 1;

    @Override
    public void continueOrPropagate(RetryableException e) {
        if (attempt++ >= maxAttempts) {
            log.error("Se alcanzó el máximo número de intentos ({}) para la llamada al core bancario", maxAttempts);
            throw e;
        }

        try {
            log.warn("Reintentando llamada al core bancario. Intento {} de {}", attempt, maxAttempts);
            Thread.sleep(backoff * attempt);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    @Override
    public Retryer clone() {
        return new CoreClientRetryer();
    }
} 