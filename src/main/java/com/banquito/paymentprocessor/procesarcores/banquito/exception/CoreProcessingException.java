package com.banquito.paymentprocessor.procesarcores.banquito.exception;

public class CoreProcessingException extends RuntimeException {
    
    private final String referencia;

    public CoreProcessingException(String message, String referencia) {
        super(message);
        this.referencia = referencia;
    }

    public String getReferencia() {
        return referencia;
    }

    @Override
    public String getMessage() {
        return String.format("%s. Referencia: %s", super.getMessage(), this.referencia);
    }
} 