package com.desafio.contaspagar.interfaces.rest;

import com.desafio.contaspagar.application.dto.*;
import com.desafio.contaspagar.application.dto.*;
import com.desafio.contaspagar.application.service.ContaService;
import com.desafio.contaspagar.application.service.CsvImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/contas")
@Tag(name = "Contas", description = "Gestão de contas a pagar")
public class ContaController {

    private final ContaService contaService;
    private final CsvImportService csvImportService;

    public ContaController(ContaService contaService, CsvImportService csvImportService) {
        this.contaService = contaService;
        this.csvImportService = csvImportService;
    }

    @PostMapping
    @Operation(summary = "Criar uma nova conta")
    public ResponseEntity<ContaResponse> criar(@Valid @RequestBody ContaRequest request) {
        ContaResponse response = contaService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar conta por ID")
    public ResponseEntity<ContaResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(contaService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar uma conta existente")
    public ResponseEntity<ContaResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ContaRequest request
    ) {
        return ResponseEntity.ok(contaService.atualizar(id, request));
    }

    @PatchMapping("/{id}/situacao")
    @Operation(summary = "Alterar situação (status) da conta")
    public ResponseEntity<ContaResponse> alterarSituacao(
            @PathVariable UUID id,
            @Valid @RequestBody AlterarSituacaoRequest request
    ) {
        return ResponseEntity.ok(contaService.alterarSituacao(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir uma conta")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        contaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Listar contas com paginação e filtros")
    public ResponseEntity<Page<ContaResponse>> listar(
            @Parameter(description = "Filtrar por data de vencimento (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataVencimento,

            @Parameter(description = "Filtrar por descrição (busca parcial)")
            @RequestParam(required = false) String descricao,

            @PageableDefault(size = 10, sort = "dataVencimento", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(contaService.listar(dataVencimento, descricao, pageable));
    }

    @GetMapping("/relatorio/total-pago")
    @Operation(summary = "Relatório de valor total pago por período")
    public ResponseEntity<TotalPagoPeriodoResponse> totalPagoPorPeriodo(
            @Parameter(description = "Data início (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,

            @Parameter(description = "Data fim (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) {
        return ResponseEntity.ok(contaService.totalPagoPorPeriodo(dataInicio, dataFim));
    }

    @PostMapping(value = "/importar-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar contas via arquivo CSV (processamento assíncrono)")
    public ResponseEntity<CsvImportResponse> importarCsv(
            @Parameter(description = "Arquivo CSV com contas. Formato: data_vencimento;valor;descricao;fornecedor_id")
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.accepted().body(csvImportService.importarCsv(file));
    }
}
