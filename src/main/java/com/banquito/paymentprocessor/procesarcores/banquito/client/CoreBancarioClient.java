package com.banquito.paymentprocessor.procesarcores.banquito.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.banquito.paymentprocessor.procesarcores.banquito.controller.dto.TransaccionCoreDTO;

@FeignClient(name = "core-bancario", url = "${core.bancario.url}")
public interface CoreBancarioClient {
    
    @PostMapping("/api/v1/transacciones")
    ResponseEntity<Void> procesarTransaccion(@RequestBody TransaccionCoreDTO transaccion);
} 