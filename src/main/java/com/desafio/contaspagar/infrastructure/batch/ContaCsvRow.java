package com.desafio.contaspagar.infrastructure.batch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO que representa uma linha do CSV durante o processamento Spring Batch.
 * Usado pelo FlatFileItemReader para mapear cada linha do arquivo.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContaCsvRow {

    private LocalDate dataVencimento;
    private BigDecimal valor;
    private String descricao;
    private Long fornecedorId;
}
