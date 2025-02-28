package com.banquito.paymentprocessor.procesarcores.banquito.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.banquito.paymentprocessor.procesarcores.banquito.controller.dto.TransaccionCoreDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.controller.dto.TransaccionCoreResponseDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.exception.CoreProcessingException;
import com.banquito.paymentprocessor.procesarcores.banquito.service.ProcesarCoreService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@ExtendWith(MockitoExtension.class)
public class ProcesarCoreControllerTest {

    private MockMvc mockMvc;
    
    @Mock
    private ProcesarCoreService service;
    
    @InjectMocks
    private ProcesarCoreController controller;
    
    private ObjectMapper objectMapper;
    
    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Para manejar LocalDateTime
    }
    
    @Test
    public void procesarTransaccion_successful() throws Exception {
        // Preparar datos de prueba
        TransaccionCoreDTO transaccionDTO = crearTransaccionDTOPrueba();
        TransaccionCoreResponseDTO responseDTO = new TransaccionCoreResponseDTO();
        responseDTO.setEstado("APROBADA");
        responseDTO.setCodigoRespuesta("00");
        responseDTO.setMensaje("Transacción procesada exitosamente");
        
        // Configurar comportamiento del mock
        when(service.procesarTransaccion(any(TransaccionCoreDTO.class))).thenReturn(responseDTO);
        
        // Ejecutar y verificar
        mockMvc.perform(post("/api/v1/core/procesar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transaccionDTO)))
                .andExpect(status().isOk());
    }
    
    @Test
    public void procesarTransaccion_coreProcessingException() throws Exception {
        // Preparar datos de prueba
        TransaccionCoreDTO transaccionDTO = crearTransaccionDTOPrueba();
        
        // Configurar comportamiento del mock
        when(service.procesarTransaccion(any(TransaccionCoreDTO.class)))
            .thenThrow(new CoreProcessingException("Error en procesamiento del core", null));
        
        // Ejecutar y verificar
        mockMvc.perform(post("/api/v1/core/procesar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transaccionDTO)))
                .andExpect(status().isUnprocessableEntity());
    }
    
    @Test
    public void procesarTransaccion_internalServerError() throws Exception {
        // Preparar datos de prueba
        TransaccionCoreDTO transaccionDTO = crearTransaccionDTOPrueba();
        
        // Configurar comportamiento del mock
        when(service.procesarTransaccion(any(TransaccionCoreDTO.class)))
            .thenThrow(new RuntimeException("Error interno"));
        
        // Ejecutar y verificar
        mockMvc.perform(post("/api/v1/core/procesar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transaccionDTO)))
                .andExpect(status().isInternalServerError());
    }
    
    private TransaccionCoreDTO crearTransaccionDTOPrueba() {
        TransaccionCoreDTO dto = new TransaccionCoreDTO();
        dto.setNumeroTarjeta("4111111111111111");
        dto.setCvv("123");
        dto.setFechaCaducidad(LocalDateTime.now().plusYears(2));
        dto.setMonto(new BigDecimal("100.00"));
        dto.setCodigoUnico("UNIQUE123");
        dto.setTipo("CORRIENTE");
        dto.setSwiftBanco("BANQECAA");
        dto.setSwiftBancoComercio("BANQECBB");
        dto.setCuentaIbanComercio("ES9121000418450200051332");
        dto.setReferencia("REF123456");
        dto.setCodigoComercio("COM001");
        return dto;
    }
} 