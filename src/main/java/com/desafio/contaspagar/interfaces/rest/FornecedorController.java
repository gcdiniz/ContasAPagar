package com.desafio.contaspagar.interfaces.rest;

import com.desafio.contaspagar.application.dto.FornecedorRequest;
import com.desafio.contaspagar.application.dto.FornecedorResponse;
import com.desafio.contaspagar.application.service.FornecedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fornecedores")
@Tag(name = "Fornecedores", description = "Gestão de fornecedores")
public class FornecedorController {

    private final FornecedorService fornecedorService;

    public FornecedorController(FornecedorService fornecedorService) {
        this.fornecedorService = fornecedorService;
    }

    @PostMapping
    @Operation(summary = "Criar um novo fornecedor")
    public ResponseEntity<FornecedorResponse> criar(@Valid @RequestBody FornecedorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fornecedorService.criar(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar fornecedor por ID")
    public ResponseEntity<FornecedorResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(fornecedorService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar um fornecedor existente")
    public ResponseEntity<FornecedorResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody FornecedorRequest request
    ) {
        return ResponseEntity.ok(fornecedorService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir um fornecedor")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        fornecedorService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Listar fornecedores com paginação")
    public ResponseEntity<Page<FornecedorResponse>> listar(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(fornecedorService.listar(pageable));
    }
}
