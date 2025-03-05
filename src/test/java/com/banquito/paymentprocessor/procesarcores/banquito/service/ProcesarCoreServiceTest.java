package com.banquito.paymentprocessor.procesarcores.banquito.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import com.banquito.paymentprocessor.procesarcores.banquito.client.CoreBancarioClient;
import com.banquito.paymentprocessor.procesarcores.banquito.client.dto.ComercioRequestDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.client.dto.CoreResponseDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.client.dto.TarjetaRequestDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.controller.dto.TransaccionCoreDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.controller.dto.TransaccionCoreResponseDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.exception.CoreProcessingException;

@ExtendWith(MockitoExtension.class)
public class ProcesarCoreServiceTest {

    @Mock
    private CoreBancarioClient coreBancarioClient;

    @Mock
    private WebClient.Builder webClientBuilder;

    @InjectMocks
    private ProcesarCoreService service;

    @BeforeEach
    public void setup() {
        // Inicialización o configuración adicional si es necesaria
    }

    @Test
    public void procesarTransaccion_exitoso() {
        CoreResponseDTO coreResponseExitoso = CoreResponseDTO.builder()
                .estado("APROBADO")
                .mensaje("Transacción exitosa")
                .codigoRespuesta("00")
                .codigoTransaccion("AUTH123")
                .build();
                
        ResponseEntity<CoreResponseDTO> responseEntity = ResponseEntity.ok(coreResponseExitoso);
                
        when(coreBancarioClient.procesarTransaccionTarjeta(any(TarjetaRequestDTO.class)))
                .thenReturn(responseEntity);
        when(coreBancarioClient.procesarTransaccionCuenta(any(ComercioRequestDTO.class)))
                .thenReturn(responseEntity);

        TransaccionCoreDTO transaccionDTO = crearTransaccionDTOPrueba();
        TransaccionCoreResponseDTO resultado = service.procesarTransaccion(transaccionDTO);

        assertNotNull(resultado);
        assertEquals("APROBADO", resultado.getEstado());
        assertTrue(resultado.getMensaje().contains("correctamente"));

        verify(coreBancarioClient, times(1)).procesarTransaccionTarjeta(any(TarjetaRequestDTO.class));
        verify(coreBancarioClient, times(1)).procesarTransaccionCuenta(any(ComercioRequestDTO.class));
    }

    @Test
    public void procesarTransaccion_errorEnDebitoTarjeta() {
        CoreResponseDTO coreResponseError = CoreResponseDTO.builder()
                .estado("RECHAZADO")
                .mensaje("Fondos insuficientes")
                .codigoRespuesta("51")
                .codigoTransaccion("")
                .build();
                
        ResponseEntity<CoreResponseDTO> responseEntity = ResponseEntity.ok(coreResponseError);

        when(coreBancarioClient.procesarTransaccionTarjeta(any(TarjetaRequestDTO.class)))
                .thenReturn(responseEntity);

        TransaccionCoreDTO transaccionDTO = crearTransaccionDTOPrueba();

        TransaccionCoreResponseDTO resultado = service.procesarTransaccion(transaccionDTO);

        assertNotNull(resultado);
        assertEquals("RECHAZADO", resultado.getEstado());
        assertTrue(resultado.getMensaje().contains("Fondos insuficientes"));

        verify(coreBancarioClient, times(1)).procesarTransaccionTarjeta(any(TarjetaRequestDTO.class));
        verify(coreBancarioClient, never()).procesarTransaccionCuenta(any(ComercioRequestDTO.class));
    }

    @Test
    public void procesarTransaccion_errorEnCreditoComercio() {
        CoreResponseDTO coreResponseExitoso = CoreResponseDTO.builder()
                .estado("APROBADO")
                .mensaje("Transacción exitosa")
                .codigoRespuesta("00")
                .codigoTransaccion("AUTH123")
                .build();
                
        CoreResponseDTO coreResponseError = CoreResponseDTO.builder()
                .estado("RECHAZADO")
                .mensaje("Error en cuenta de comercio")
                .codigoRespuesta("15")
                .codigoTransaccion("")
                .build();
                
        ResponseEntity<CoreResponseDTO> responseExitoso = ResponseEntity.ok(coreResponseExitoso);
        ResponseEntity<CoreResponseDTO> responseError = ResponseEntity.ok(coreResponseError);
                
        when(coreBancarioClient.procesarTransaccionTarjeta(any(TarjetaRequestDTO.class)))
                .thenReturn(responseExitoso);
                
        when(coreBancarioClient.procesarTransaccionCuenta(any(ComercioRequestDTO.class)))
                .thenReturn(responseError);

        TransaccionCoreDTO transaccionDTO = crearTransaccionDTOPrueba();

        TransaccionCoreResponseDTO resultado = service.procesarTransaccion(transaccionDTO);

        assertNotNull(resultado);
        assertEquals("RECHAZADO", resultado.getEstado());
        assertTrue(resultado.getMensaje().contains("Error en cuenta de comercio"));

        verify(coreBancarioClient, times(1)).procesarTransaccionTarjeta(any(TarjetaRequestDTO.class));
        verify(coreBancarioClient, times(1)).procesarTransaccionCuenta(any(ComercioRequestDTO.class));
    }

    @Test
    public void procesarTransaccion_excepcionEnCliente() {
        when(coreBancarioClient.procesarTransaccionTarjeta(any(TarjetaRequestDTO.class)))
                .thenThrow(new RuntimeException("Error de conexión"));

        TransaccionCoreDTO transaccionDTO = crearTransaccionDTOPrueba();
        TransaccionCoreResponseDTO resultado = service.procesarTransaccion(transaccionDTO);

        assertNotNull(resultado);
        assertEquals("RECHAZADO", resultado.getEstado());
        assertTrue(resultado.getMensaje().contains("Error de conexión"));
    }

    private TransaccionCoreDTO crearTransaccionDTOPrueba() {
        TransaccionCoreDTO dto = new TransaccionCoreDTO();
        dto.setCodTransaccion("TRX123456");
        dto.setCodigoUnico("UNIQUE123");
        dto.setCodigoGtw("PAYPAL");
        dto.setNumeroTarjeta("4111111111111111");
        dto.setCvv("123");
        dto.setFechaCaducidad("12/25");
        dto.setMonto(new BigDecimal("100.00"));
        dto.setCodigoMoneda("USD");
        dto.setTipo("COM");
        dto.setSwiftBancoTarjeta("BANQECAA");
        dto.setSwiftBancoComercio("BANQECBB");
        dto.setCuentaIbanComercio("ES9121000418450200051332");
        dto.setReferencia("REF123456");
        dto.setPais("EC");
        dto.setMarca("VISA");
        return dto;
    }
} 