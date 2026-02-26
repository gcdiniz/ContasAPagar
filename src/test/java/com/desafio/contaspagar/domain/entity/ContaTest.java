package com.desafio.contaspagar.domain.entity;

import com.desafio.contaspagar.domain.enums.SituacaoConta;
import com.desafio.contaspagar.domain.exception.TransicaoSituacaoInvalidaException;
import com.desafio.contaspagar.domain.exception.ValorInvalidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ContaTest {

    private Fornecedor fornecedor;

    @BeforeEach
    void setUp() {
        fornecedor = new Fornecedor("Fornecedor Teste");
    }

    // --- Criação de Conta ---

    @Test
    @DisplayName("Deve criar conta com dados válidos")
    void deveCriarContaComDadosValidos() {
        Conta conta = new Conta(
                LocalDate.of(2026, 3, 15),
                new BigDecimal("1500.00"),
                "Conta de teste",
                fornecedor
        );

        assertNotNull(conta);
        assertEquals(LocalDate.of(2026, 3, 15), conta.getDataVencimento());
        assertEquals(new BigDecimal("1500.00"), conta.getValor());
        assertEquals("Conta de teste", conta.getDescricao());
        assertEquals(SituacaoConta.PENDENTE, conta.getSituacao());
        assertNull(conta.getDataPagamento());
        assertEquals(fornecedor, conta.getFornecedor());
    }

    @Test
    @DisplayName("Deve lançar exceção para valor nulo")
    void deveLancarExcecaoParaValorNulo() {
        assertThrows(ValorInvalidoException.class, () ->
                new Conta(LocalDate.now(), null, "Teste", fornecedor)
        );
    }

    @Test
    @DisplayName("Deve lançar exceção para valor zero")
    void deveLancarExcecaoParaValorZero() {
        assertThrows(ValorInvalidoException.class, () ->
                new Conta(LocalDate.now(), BigDecimal.ZERO, "Teste", fornecedor)
        );
    }

    @Test
    @DisplayName("Deve lançar exceção para valor negativo")
    void deveLancarExcecaoParaValorNegativo() {
        assertThrows(ValorInvalidoException.class, () ->
                new Conta(LocalDate.now(), new BigDecimal("-100"), "Teste", fornecedor)
        );
    }

    @Test
    @DisplayName("Deve lançar exceção para descrição vazia")
    void deveLancarExcecaoParaDescricaoVazia() {
        assertThrows(ValorInvalidoException.class, () ->
                new Conta(LocalDate.now(), new BigDecimal("100"), "", fornecedor)
        );
    }

    @Test
    @DisplayName("Deve lançar exceção para descrição nula")
    void deveLancarExcecaoParaDescricaoNula() {
        assertThrows(ValorInvalidoException.class, () ->
                new Conta(LocalDate.now(), new BigDecimal("100"), null, fornecedor)
        );
    }

    @Test
    @DisplayName("Deve lançar exceção para descrição com mais de 500 caracteres")
    void deveLancarExcecaoParaDescricaoMuitoLonga() {
        String descricaoLonga = "A".repeat(501);
        assertThrows(ValorInvalidoException.class, () ->
                new Conta(LocalDate.now(), new BigDecimal("100"), descricaoLonga, fornecedor)
        );
    }

    @Test
    @DisplayName("Deve lançar exceção para data de vencimento nula")
    void deveLancarExcecaoParaDataVencimentoNula() {
        assertThrows(NullPointerException.class, () ->
                new Conta(null, new BigDecimal("100"), "Teste", fornecedor)
        );
    }

    @Test
    @DisplayName("Deve lançar exceção para fornecedor nulo")
    void deveLancarExcecaoParaFornecedorNulo() {
        assertThrows(NullPointerException.class, () ->
                new Conta(LocalDate.now(), new BigDecimal("100"), "Teste", null)
        );
    }

    // --- Alteração de Situação ---

    @Test
    @DisplayName("Deve alterar situação de PENDENTE para PAGO")
    void deveAlterarSituacaoParaPago() {
        Conta conta = criarContaPendente();
        conta.alterarSituacao(SituacaoConta.PAGO);

        assertEquals(SituacaoConta.PAGO, conta.getSituacao());
        assertNotNull(conta.getDataPagamento());
        assertEquals(LocalDate.now(), conta.getDataPagamento());
    }

    @Test
    @DisplayName("Deve alterar situação de PENDENTE para CANCELADO")
    void deveAlterarSituacaoParaCancelado() {
        Conta conta = criarContaPendente();
        conta.alterarSituacao(SituacaoConta.CANCELADO);

        assertEquals(SituacaoConta.CANCELADO, conta.getSituacao());
    }

    @Test
    @DisplayName("Não deve permitir alterar de PAGO para PENDENTE")
    void naoDevePermitirAlterarDePagoParaPendente() {
        Conta conta = criarContaPendente();
        conta.alterarSituacao(SituacaoConta.PAGO);

        assertThrows(TransicaoSituacaoInvalidaException.class, () ->
                conta.alterarSituacao(SituacaoConta.PENDENTE)
        );
    }

    @Test
    @DisplayName("Não deve permitir alterar de CANCELADO para PAGO")
    void naoDevePermitirAlterarDeCanceladoParaPago() {
        Conta conta = criarContaPendente();
        conta.alterarSituacao(SituacaoConta.CANCELADO);

        assertThrows(TransicaoSituacaoInvalidaException.class, () ->
                conta.alterarSituacao(SituacaoConta.PAGO)
        );
    }

    @Test
    @DisplayName("Não deve permitir alterar de PAGO para CANCELADO")
    void naoDevePermitirAlterarDePagoParaCancelado() {
        Conta conta = criarContaPendente();
        conta.alterarSituacao(SituacaoConta.PAGO);

        assertThrows(TransicaoSituacaoInvalidaException.class, () ->
                conta.alterarSituacao(SituacaoConta.CANCELADO)
        );
    }

    // --- Atualização de Conta ---

    @Test
    @DisplayName("Deve permitir atualizar conta PENDENTE")
    void devePermitirAtualizarContaPendente() {
        Conta conta = criarContaPendente();
        Fornecedor novoFornecedor = new Fornecedor("Novo Fornecedor");

        conta.atualizar(
                LocalDate.of(2026, 4, 1),
                new BigDecimal("2000.00"),
                "Descrição atualizada",
                novoFornecedor
        );

        assertEquals(LocalDate.of(2026, 4, 1), conta.getDataVencimento());
        assertEquals(new BigDecimal("2000.00"), conta.getValor());
        assertEquals("Descrição atualizada", conta.getDescricao());
        assertEquals(novoFornecedor, conta.getFornecedor());
    }

    @Test
    @DisplayName("Não deve permitir atualizar conta PAGA")
    void naoDevePermitirAtualizarContaPaga() {
        Conta conta = criarContaPendente();
        conta.alterarSituacao(SituacaoConta.PAGO);

        assertThrows(TransicaoSituacaoInvalidaException.class, () ->
                conta.atualizar(LocalDate.now(), new BigDecimal("100"), "Teste", fornecedor)
        );
    }

    @Test
    @DisplayName("Não deve permitir atualizar conta CANCELADA")
    void naoDevePermitirAtualizarContaCancelada() {
        Conta conta = criarContaPendente();
        conta.alterarSituacao(SituacaoConta.CANCELADO);

        assertThrows(TransicaoSituacaoInvalidaException.class, () ->
                conta.atualizar(LocalDate.now(), new BigDecimal("100"), "Teste", fornecedor)
        );
    }

    @Test
    @DisplayName("Não deve permitir atualizar com valor negativo")
    void naoDevePermitirAtualizarComValorNegativo() {
        Conta conta = criarContaPendente();

        assertThrows(ValorInvalidoException.class, () ->
                conta.atualizar(LocalDate.now(), new BigDecimal("-50"), "Teste", fornecedor)
        );
    }

    private Conta criarContaPendente() {
        return new Conta(
                LocalDate.of(2026, 3, 15),
                new BigDecimal("1500.00"),
                "Conta de teste",
                fornecedor
        );
    }
}
