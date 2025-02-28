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
        CoreResponseDTO coreResponseExitoso = new CoreResponseDTO();
        coreResponseExitoso.setTransaccionExitosa(true);
        coreResponseExitoso.setCodigoAutorizacion("AUTH123");
        coreResponseExitoso.setMensaje("Transacción exitosa");
        coreResponseExitoso.setEstado("APROBADA");

        when(coreBancarioClient.procesarTransaccionTarjeta(any(TarjetaRequestDTO.class)))
                .thenReturn(coreResponseExitoso);
        when(coreBancarioClient.procesarTransaccionComercio(any(ComercioRequestDTO.class)))
                .thenReturn(coreResponseExitoso);

        TransaccionCoreDTO transaccionDTO = crearTransaccionDTOPrueba();
        TransaccionCoreResponseDTO resultado = service.procesarTransaccion(transaccionDTO);

        assertNotNull(resultado);
        assertEquals("APROBADA", resultado.getEstado());
        assertEquals("00", resultado.getCodigoRespuesta());
        assertTrue(resultado.getMensaje().contains("exitosa"));

        verify(coreBancarioClient, times(1)).procesarTransaccionTarjeta(any(TarjetaRequestDTO.class));
        verify(coreBancarioClient, times(1)).procesarTransaccionComercio(any(ComercioRequestDTO.class));
    }

    @Test
    public void procesarTransaccion_errorEnDebitoTarjeta() {
        CoreResponseDTO coreResponseError = new CoreResponseDTO();
        coreResponseError.setTransaccionExitosa(false);
        coreResponseError.setCodigoError("51");
        coreResponseError.setMensaje("Fondos insuficientes");
        coreResponseError.setEstado("RECHAZADA");

        when(coreBancarioClient.procesarTransaccionTarjeta(any(TarjetaRequestDTO.class)))
                .thenReturn(coreResponseError);

        TransaccionCoreDTO transaccionDTO = crearTransaccionDTOPrueba();

        CoreProcessingException exception = assertThrows(CoreProcessingException.class, () -> {
            service.procesarTransaccion(transaccionDTO);
        });

        assertTrue(exception.getMessage().contains("Fondos insuficientes"));
        verify(coreBancarioClient, times(1)).procesarTransaccionTarjeta(any(TarjetaRequestDTO.class));
        verify(coreBancarioClient, never()).procesarTransaccionComercio(any(ComercioRequestDTO.class));
    }

    @Test
    public void procesarTransaccion_errorEnCreditoComercio() {
        CoreResponseDTO coreResponseExitoso = new CoreResponseDTO();
        coreResponseExitoso.setTransaccionExitosa(true);
        coreResponseExitoso.setCodigoAutorizacion("AUTH123");
        coreResponseExitoso.setMensaje("Transacción exitosa");
        coreResponseExitoso.setEstado("APROBADA");
        
        CoreResponseDTO coreResponseError = new CoreResponseDTO();
        coreResponseError.setTransaccionExitosa(false);
        coreResponseError.setCodigoError("15");
        coreResponseError.setMensaje("Error en comercio");
        coreResponseError.setEstado("RECHAZADA");

        when(coreBancarioClient.procesarTransaccionTarjeta(any(TarjetaRequestDTO.class)))
                .thenReturn(coreResponseExitoso);
                
        when(coreBancarioClient.procesarTransaccionComercio(any(ComercioRequestDTO.class)))
                .thenReturn(coreResponseError);

        TransaccionCoreDTO transaccionDTO = crearTransaccionDTOPrueba();

        CoreProcessingException exception = assertThrows(CoreProcessingException.class, () -> {
            service.procesarTransaccion(transaccionDTO);
        });

        verify(coreBancarioClient, times(1)).procesarTransaccionTarjeta(any(TarjetaRequestDTO.class));
        verify(coreBancarioClient, times(1)).procesarTransaccionComercio(any(ComercioRequestDTO.class));
    }

    @Test
    public void procesarTransaccion_excepcionEnCliente() {
        when(coreBancarioClient.procesarTransaccionTarjeta(any(TarjetaRequestDTO.class)))
                .thenThrow(new RuntimeException("Error de conexión"));

        TransaccionCoreDTO transaccionDTO = crearTransaccionDTOPrueba();

        CoreProcessingException exception = assertThrows(CoreProcessingException.class, () -> {
            service.procesarTransaccion(transaccionDTO);
        });

        assertTrue(exception.getMessage().contains("Error"));
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