package com.banquito.paymentprocessor.procesarcores.banquito.client;

import static org.junit.jupiter.api.Assertions.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.banquito.paymentprocessor.procesarcores.banquito.client.dto.ComercioRequestDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.client.dto.CoreResponseDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.client.dto.TarjetaRequestDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.exception.CoreProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.http.MediaType;

@SpringBootTest
@ActiveProfiles("test")
public class CoreBancarioClientTest {

    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .build();

    @Autowired
    private CoreBancarioClient coreBancarioClient;

    private ObjectMapper objectMapper = new ObjectMapper();

    @DynamicPropertySource
    static void configurarPropiedades(DynamicPropertyRegistry registry) {
        registry.add("core.bancario.url", () -> wireMockServer.baseUrl());
    }

    @BeforeEach
    public void configurarStubs() {
        wireMockServer.resetAll();
    }

    @Test
    public void procesarTransaccionTarjeta_respuestaExitosa() throws Exception {
        CoreResponseDTO respuestaExitosa = new CoreResponseDTO();
        respuestaExitosa.setTransaccionExitosa(true);
        respuestaExitosa.setCodigoAutorizacion("AUTH123");
        respuestaExitosa.setMensaje("Transacción aprobada");
        respuestaExitosa.setEstado("APROBADA");

        wireMockServer.stubFor(post(urlEqualTo("/api/v1/transacciones/tarjeta"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(respuestaExitosa))));

        TarjetaRequestDTO requestDTO = new TarjetaRequestDTO();
        requestDTO.setNumeroTarjeta("4111111111111111");
        requestDTO.setCvv("123");
        requestDTO.setFechaCaducidad(LocalDateTime.now().plusYears(2));
        requestDTO.setMonto(new BigDecimal("100.00"));
        requestDTO.setReferencia("REF123456");

        CoreResponseDTO resultado = coreBancarioClient.procesarTransaccionTarjeta(requestDTO);

        assertNotNull(resultado);
        assertTrue(resultado.getTransaccionExitosa());
        assertEquals("AUTH123", resultado.getCodigoAutorizacion());
        assertEquals("APROBADA", resultado.getEstado());
    }

    @Test
    public void procesarTransaccionComercio_respuestaExitosa() throws Exception {
        CoreResponseDTO respuestaExitosa = new CoreResponseDTO();
        respuestaExitosa.setTransaccionExitosa(true);
        respuestaExitosa.setCodigoAutorizacion("AUTH456");
        respuestaExitosa.setMensaje("Crédito aprobado");
        respuestaExitosa.setEstado("APROBADA");

        wireMockServer.stubFor(post(urlEqualTo("/api/v1/transacciones/comercio"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(respuestaExitosa))));

        ComercioRequestDTO requestDTO = new ComercioRequestDTO();
        requestDTO.setCodigoComercio("COM001");
        requestDTO.setMonto(new BigDecimal("100.00"));
        requestDTO.setCuentaIbanComercio("ES9121000418450200051332");
        requestDTO.setSwiftBancoComercio("BANQECAA");
        requestDTO.setReferencia("REF123456");

        CoreResponseDTO resultado = coreBancarioClient.procesarTransaccionComercio(requestDTO);

        assertNotNull(resultado);
        assertTrue(resultado.getTransaccionExitosa());
        assertEquals("AUTH456", resultado.getCodigoAutorizacion());
        assertEquals("APROBADA", resultado.getEstado());
    }

    @Test
    public void procesarTransaccionTarjeta_respuestaError() throws Exception {
        CoreResponseDTO respuestaError = new CoreResponseDTO();
        respuestaError.setTransaccionExitosa(false);
        respuestaError.setCodigoError("51");
        respuestaError.setMensaje("Fondos insuficientes");
        respuestaError.setEstado("RECHAZADA");

        wireMockServer.stubFor(post(urlEqualTo("/api/v1/transacciones/tarjeta"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(respuestaError))));

        TarjetaRequestDTO requestDTO = new TarjetaRequestDTO();
        requestDTO.setNumeroTarjeta("4111111111111111");
        requestDTO.setCvv("123");
        requestDTO.setFechaCaducidad(LocalDateTime.now().plusYears(2));
        requestDTO.setMonto(new BigDecimal("10000.00"));
        requestDTO.setReferencia("REF123456");

        CoreResponseDTO resultado = coreBancarioClient.procesarTransaccionTarjeta(requestDTO);

        assertNotNull(resultado);
        assertFalse(resultado.getTransaccionExitosa());
        assertEquals("51", resultado.getCodigoError());
        assertEquals("RECHAZADA", resultado.getEstado());
    }

    @Test
    public void procesarTransaccionComercio_respuestaError() throws Exception {
        CoreResponseDTO respuestaError = new CoreResponseDTO();
        respuestaError.setTransaccionExitosa(false);
        respuestaError.setCodigoError("15");
        respuestaError.setMensaje("Cuenta de comercio inválida");
        respuestaError.setEstado("RECHAZADA");

        wireMockServer.stubFor(post(urlEqualTo("/api/v1/transacciones/comercio"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(respuestaError))));

        ComercioRequestDTO requestDTO = new ComercioRequestDTO();
        requestDTO.setCodigoComercio("COM001");
        requestDTO.setMonto(new BigDecimal("100.00"));
        requestDTO.setCuentaIbanComercio("ES9121000418450200999999");
        requestDTO.setSwiftBancoComercio("BANQECAA");
        requestDTO.setReferencia("REF123456");

        CoreResponseDTO resultado = coreBancarioClient.procesarTransaccionComercio(requestDTO);

        assertNotNull(resultado);
        assertFalse(resultado.getTransaccionExitosa());
        assertEquals("15", resultado.getCodigoError());
        assertEquals("RECHAZADA", resultado.getEstado());
    }
} 