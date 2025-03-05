package com.banquito.paymentprocessor.procesarcores.banquito.client;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.banquito.paymentprocessor.procesarcores.banquito.client.dto.ComercioRequestDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.client.dto.CoreResponseDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.client.dto.TarjetaRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

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
        registry.add("app.core-bancario.url", () -> wireMockServer.baseUrl());
    }

    @BeforeEach
    public void configurarStubs() {
        wireMockServer.resetAll();
    }

    @Test
    public void procesarTransaccionTarjeta_respuestaExitosa() throws Exception {
        CoreResponseDTO respuestaExitosa = CoreResponseDTO.builder()
                .estado("APROBADO")
                .mensaje("Transacción aprobada")
                .codigoRespuesta("00")
                .codigoTransaccion("AUTH123")
                .build();

        wireMockServer.stubFor(post(urlEqualTo("/v1/transacciones/tarjeta"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(respuestaExitosa))));

        TarjetaRequestDTO requestDTO = TarjetaRequestDTO.builder()
                .numeroTarjeta("4111111111111111")
                .tipo("COMPRA")
                .monto(new BigDecimal("100.00"))
                .moneda("USD")
                .pais("ECU")
                .swift("BANECUXXXX")
                .codigoUnicoTransaccion("TX123456")
                .referencia("REF123456")
                .transaccionEncriptada("TX123456")
                .diferido(false)
                .cuotas(1)
                .build();

        ResponseEntity<CoreResponseDTO> respuesta = coreBancarioClient.procesarTransaccionTarjeta(requestDTO);

        assertNotNull(respuesta);
        assertNotNull(respuesta.getBody());
        assertEquals("APROBADO", respuesta.getBody().getEstado());
        assertEquals("Transacción aprobada", respuesta.getBody().getMensaje());
        assertEquals("00", respuesta.getBody().getCodigoRespuesta());
    }

    @Test
    public void procesarTransaccionCuenta_respuestaExitosa() throws Exception {
        CoreResponseDTO respuestaExitosa = CoreResponseDTO.builder()
                .estado("APROBADO")
                .mensaje("Crédito aprobado")
                .codigoRespuesta("00")
                .codigoTransaccion("AUTH456")
                .build();

        wireMockServer.stubFor(post(urlEqualTo("/v1/transacciones/cuenta"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(respuestaExitosa))));

        ComercioRequestDTO requestDTO = ComercioRequestDTO.builder()
                .iban("ES9121000418450200051332")
                .swift("BANQECAA")
                .tipo("COMPRA")
                .codigoUnico("TX123456")
                .monto(new BigDecimal("100.00"))
                .referencia("REF123456")
                .build();

        ResponseEntity<CoreResponseDTO> respuesta = coreBancarioClient.procesarTransaccionCuenta(requestDTO);

        assertNotNull(respuesta);
        assertNotNull(respuesta.getBody());
        assertEquals("APROBADO", respuesta.getBody().getEstado());
        assertEquals("Crédito aprobado", respuesta.getBody().getMensaje());
        assertEquals("00", respuesta.getBody().getCodigoRespuesta());
    }

    @Test
    public void procesarTransaccionTarjeta_respuestaError() throws Exception {
        CoreResponseDTO respuestaError = CoreResponseDTO.builder()
                .estado("RECHAZADO")
                .mensaje("Fondos insuficientes")
                .codigoRespuesta("51")
                .codigoTransaccion("")
                .build();

        wireMockServer.stubFor(post(urlEqualTo("/v1/transacciones/tarjeta"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(respuestaError))));

        TarjetaRequestDTO requestDTO = TarjetaRequestDTO.builder()
                .numeroTarjeta("4111111111111111")
                .tipo("COMPRA")
                .monto(new BigDecimal("10000.00"))
                .moneda("USD")
                .pais("ECU")
                .swift("BANECUXXXX")
                .codigoUnicoTransaccion("TX123456")
                .referencia("REF123456")
                .transaccionEncriptada("TX123456")
                .diferido(false)
                .cuotas(1)
                .build();

        ResponseEntity<CoreResponseDTO> respuesta = coreBancarioClient.procesarTransaccionTarjeta(requestDTO);

        assertNotNull(respuesta);
        assertNotNull(respuesta.getBody());
        assertEquals("RECHAZADO", respuesta.getBody().getEstado());
        assertEquals("Fondos insuficientes", respuesta.getBody().getMensaje());
        assertEquals("51", respuesta.getBody().getCodigoRespuesta());
    }

    @Test
    public void procesarTransaccionCuenta_respuestaError() throws Exception {
        CoreResponseDTO respuestaError = CoreResponseDTO.builder()
                .estado("RECHAZADO")
                .mensaje("Cuenta de comercio inválida")
                .codigoRespuesta("15")
                .codigoTransaccion("")
                .build();

        wireMockServer.stubFor(post(urlEqualTo("/v1/transacciones/cuenta"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(respuestaError))));

        ComercioRequestDTO requestDTO = ComercioRequestDTO.builder()
                .iban("ES9121000418450200999999")
                .swift("BANQECAA")
                .tipo("COMPRA")
                .codigoUnico("TX123456")
                .monto(new BigDecimal("100.00"))
                .referencia("REF123456")
                .build();

        ResponseEntity<CoreResponseDTO> respuesta = coreBancarioClient.procesarTransaccionCuenta(requestDTO);

        assertNotNull(respuesta);
        assertNotNull(respuesta.getBody());
        assertEquals("RECHAZADO", respuesta.getBody().getEstado());
        assertEquals("Cuenta de comercio inválida", respuesta.getBody().getMensaje());
        assertEquals("15", respuesta.getBody().getCodigoRespuesta());
    }
} 