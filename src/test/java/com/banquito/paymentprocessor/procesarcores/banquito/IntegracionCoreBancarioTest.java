package com.banquito.paymentprocessor.procesarcores.banquito;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.banquito.paymentprocessor.procesarcores.banquito.client.dto.CoreResponseDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.controller.dto.TransaccionCoreDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class IntegracionCoreBancarioTest {

    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().dynamicPort())
            .build();
    
    @Autowired
    private MockMvc mockMvc;
    
    private ObjectMapper objectMapper;
    
    @DynamicPropertySource
    static void configurarPropiedades(DynamicPropertyRegistry registry) {
        registry.add("app.core-bancario.url", wireMockServer::baseUrl);
    }
    
    @BeforeEach
    public void setup() {
        wireMockServer.resetAll();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }
    
    @Test
    public void procesarTransaccion_flujoExitoso() throws Exception {
        CoreResponseDTO respuestaTarjeta = CoreResponseDTO.builder()
                .estado("APROBADO")
                .mensaje("Débito procesado correctamente")
                .codigoRespuesta("00")
                .codigoTransaccion("AUTH123")
                .build();
        
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/v1/transacciones/tarjeta"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(respuestaTarjeta))));
        
        CoreResponseDTO respuestaCuenta = CoreResponseDTO.builder()
                .estado("APROBADO")
                .mensaje("Crédito procesado correctamente")
                .codigoRespuesta("00")
                .codigoTransaccion("COM456")
                .build();
        
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/v1/transacciones/cuenta"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(respuestaCuenta))));
        
        TransaccionCoreDTO transaccionDTO = new TransaccionCoreDTO();
        transaccionDTO.setCodTransaccion("TRX123456");
        transaccionDTO.setCodigoUnico("UNIQUE123");
        transaccionDTO.setCodigoGtw("PAYPAL");
        transaccionDTO.setNumeroTarjeta("4111111111111111");
        transaccionDTO.setCvv("123");
        transaccionDTO.setFechaCaducidad("12/25");
        transaccionDTO.setMonto(new BigDecimal("100.00"));
        transaccionDTO.setCodigoMoneda("USD");
        transaccionDTO.setMarca("VISA");
        transaccionDTO.setEstado("PEN");
        transaccionDTO.setTipo("COM");
        transaccionDTO.setSwiftBancoTarjeta("BANQECAA");
        transaccionDTO.setSwiftBancoComercio("BANQECBB");
        transaccionDTO.setCuentaIbanComercio("ES9121000418450200051332");
        transaccionDTO.setReferencia("REF123456");
        transaccionDTO.setPais("EC");
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/core/procesar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transaccionDTO)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"))
                .andExpect(jsonPath("$.codigoRespuesta").value("00"));
        
        wireMockServer.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/v1/transacciones/tarjeta")));
        wireMockServer.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/v1/transacciones/cuenta")));
    }
    
    @Test
    public void procesarTransaccion_errorEnDebito() throws Exception {
        CoreResponseDTO respuestaError = CoreResponseDTO.builder()
                .estado("RECHAZADO")
                .mensaje("Fondos insuficientes")
                .codigoRespuesta("51")
                .codigoTransaccion("")
                .build();
        
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/v1/transacciones/tarjeta"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(respuestaError))));
        
        TransaccionCoreDTO transaccionDTO = new TransaccionCoreDTO();
        transaccionDTO.setCodTransaccion("TRX123456");
        transaccionDTO.setCodigoUnico("UNIQUE456");
        transaccionDTO.setCodigoGtw("PAYPAL");
        transaccionDTO.setNumeroTarjeta("4111111111111111");
        transaccionDTO.setCvv("123");
        transaccionDTO.setFechaCaducidad("12/25");
        transaccionDTO.setMonto(new BigDecimal("10000.00"));
        transaccionDTO.setCodigoMoneda("USD");
        transaccionDTO.setMarca("VISA");
        transaccionDTO.setEstado("PEN");
        transaccionDTO.setTipo("COM");
        transaccionDTO.setSwiftBancoTarjeta("BANQECAA");
        transaccionDTO.setSwiftBancoComercio("BANQECBB");
        transaccionDTO.setCuentaIbanComercio("ES9121000418450200051332");
        transaccionDTO.setReferencia("REF123456");
        transaccionDTO.setPais("EC");
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/core/procesar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transaccionDTO)))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("Fondos insuficientes")));
        
        wireMockServer.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/v1/transacciones/tarjeta")));
        wireMockServer.verify(0, WireMock.postRequestedFor(WireMock.urlEqualTo("/v1/transacciones/cuenta")));
    }
} 