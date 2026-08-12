package com.mesaayuda.llamada;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mesaayuda.llamada.dto.LlamadaCrearRequest;
import com.mesaayuda.llamada.dto.LlamadaDetalleDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/llamadas")
public class LlamadaController {

    private final LlamadaService llamadaService;

    public LlamadaController(LlamadaService llamadaService) {
        this.llamadaService = llamadaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LlamadaDetalleDto crear(@Valid @RequestBody LlamadaCrearRequest request) {
        return llamadaService.crear(request);
    }
}
