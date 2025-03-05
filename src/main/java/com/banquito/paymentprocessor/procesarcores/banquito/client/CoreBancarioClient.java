package com.banquito.paymentprocessor.procesarcores.banquito.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.banquito.paymentprocessor.procesarcores.banquito.client.dto.ComercioRequestDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.client.dto.CoreResponseDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.client.dto.TarjetaRequestDTO;

@FeignClient(name = "core-bancario", url = "${core.bancario.url}")
public interface CoreBancarioClient {
    
    @PostMapping("/v1/transacciones/tarjeta")
    ResponseEntity<CoreResponseDTO> procesarTransaccionTarjeta(@RequestBody TarjetaRequestDTO request);
    
    @PostMapping("/v1/transacciones/cuenta")
    ResponseEntity<CoreResponseDTO> procesarTransaccionCuenta(@RequestBody ComercioRequestDTO request);
} 