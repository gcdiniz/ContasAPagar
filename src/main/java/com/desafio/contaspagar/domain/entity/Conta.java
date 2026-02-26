package com.desafio.contaspagar.domain.entity;

import com.desafio.contaspagar.domain.enums.SituacaoConta;
import com.desafio.contaspagar.domain.exception.TransicaoSituacaoInvalidaException;
import com.desafio.contaspagar.domain.exception.ValorInvalidoException;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "conta")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Column(nullable = false)
    private SituacaoConta situacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id", nullable = false)
    private Fornecedor fornecedor;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Conta(LocalDate dataVencimento, BigDecimal valor, String descricao, Fornecedor fornecedor) {
        validarValor(valor);
        validarDescricao(descricao);
        Objects.requireNonNull(dataVencimento, "Data de vencimento é obrigatória");
        Objects.requireNonNull(fornecedor, "Fornecedor é obrigatório");

        this.dataVencimento = dataVencimento;
        this.valor = valor;
        this.descricao = descricao.trim();
        this.fornecedor = fornecedor;
        this.situacao = SituacaoConta.PENDENTE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // --- Invariantes de Domínio ---

    private void validarValor(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValorInvalidoException("Valor da conta deve ser positivo");
        }
    }

    private void validarDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            throw new ValorInvalidoException("Descrição é obrigatória");
        }
        if (descricao.trim().length() > 500) {
            throw new ValorInvalidoException("Descrição deve ter no máximo 500 caracteres");
        }
    }

    public void alterarSituacao(SituacaoConta novaSituacao) {
        if (!this.situacao.podeTransicionar(novaSituacao)) {
            throw new TransicaoSituacaoInvalidaException(
                    "Transição de %s para %s não é permitida".formatted(this.situacao, novaSituacao)
            );
        }
        this.situacao = novaSituacao;
        if (novaSituacao == SituacaoConta.PAGO) {
            this.dataPagamento = LocalDate.now();
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void atualizar(LocalDate dataVencimento, BigDecimal valor, String descricao, Fornecedor fornecedor) {
        if (this.situacao != SituacaoConta.PENDENTE) {
            throw new TransicaoSituacaoInvalidaException(
                    "Não é possível alterar uma conta com situação " + this.situacao
            );
        }
        validarValor(valor);
        validarDescricao(descricao);
        Objects.requireNonNull(dataVencimento, "Data de vencimento é obrigatória");
        Objects.requireNonNull(fornecedor, "Fornecedor é obrigatório");

        this.dataVencimento = dataVencimento;
        this.valor = valor;
        this.descricao = descricao.trim();
        this.fornecedor = fornecedor;
        this.updatedAt = LocalDateTime.now();
    }

}
