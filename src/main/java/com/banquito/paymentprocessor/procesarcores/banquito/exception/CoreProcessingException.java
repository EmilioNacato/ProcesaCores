package com.banquito.paymentprocessor.procesarcores.banquito.exception;

/**
 * Excepción específica para errores durante el procesamiento de transacciones en el core bancario.
 * Permite identificar y manejar de forma específica los errores relacionados con el core.
 */
public class CoreProcessingException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    private String errorCode;

    /**
     * Constructor con mensaje de error
     *
     * @param message Mensaje descriptivo del error
     */
    public CoreProcessingException(String message) {
        super(message);
    }

    /**
     * Constructor con mensaje de error y código de error
     *
     * @param message Mensaje descriptivo del error
     * @param errorCode Código de error asociado
     */
    public CoreProcessingException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Constructor con mensaje de error y causa raíz
     *
     * @param message Mensaje descriptivo del error
     * @param cause Causa raíz de la excepción
     */
    public CoreProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Obtiene el código de error asociado
     * 
     * @return Código de error o null si no existe
     */
    public String getErrorCode() {
        return errorCode;
    }
} 