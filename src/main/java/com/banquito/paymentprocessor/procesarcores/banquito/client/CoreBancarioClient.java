package com.banquito.paymentprocessor.procesarcores.banquito.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.banquito.paymentprocessor.procesarcores.banquito.client.dto.TarjetaRequestDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.client.dto.ComercioRequestDTO;
import com.banquito.paymentprocessor.procesarcores.banquito.client.dto.CoreResponseDTO;

@FeignClient(name = "core-bancario", url = "${core.bancario.url}")
public interface CoreBancarioClient {
    
    @PostMapping("/api/v1/transacciones/tarjeta")
    CoreResponseDTO procesarTransaccionTarjeta(@RequestBody TarjetaRequestDTO request);

    @PostMapping("/api/v1/transacciones/comercio")
    CoreResponseDTO procesarTransaccionComercio(@RequestBody ComercioRequestDTO request);
} 