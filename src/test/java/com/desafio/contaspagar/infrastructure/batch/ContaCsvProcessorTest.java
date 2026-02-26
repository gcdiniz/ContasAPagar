package com.desafio.contaspagar.infrastructure.batch;

import com.desafio.contaspagar.domain.entity.Conta;
import com.desafio.contaspagar.domain.entity.Fornecedor;
import com.desafio.contaspagar.infrastructure.repository.FornecedorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContaCsvProcessor")
class ContaCsvProcessorTest {

    @Mock
    private FornecedorRepository fornecedorRepository;

    @InjectMocks
    private ContaCsvProcessor processor;

    @Test
    @DisplayName("Deve converter ContaCsvRow em Conta com sucesso")
    void deveConverterCsvRowEmConta() throws Exception {
        Fornecedor fornecedor = new Fornecedor("Fornecedor Alpha");
        when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedor));

        ContaCsvRow row = new ContaCsvRow(
                LocalDate.of(2026, 3, 15),
                new BigDecimal("1500.00"),
                "Aluguel escritório",
                1L
        );

        Conta conta = processor.process(row);

        assertNotNull(conta);
        assertEquals(LocalDate.of(2026, 3, 15), conta.getDataVencimento());
        assertEquals(new BigDecimal("1500.00"), conta.getValor());
        assertEquals("Aluguel escritório", conta.getDescricao());
        assertEquals(fornecedor, conta.getFornecedor());
    }

    @Test
    @DisplayName("Deve lançar exceção para fornecedor inexistente")
    void deveLancarExcecaoParaFornecedorInexistente() {
        when(fornecedorRepository.findById(999L)).thenReturn(Optional.empty());

        ContaCsvRow row = new ContaCsvRow(
                LocalDate.of(2026, 3, 15),
                new BigDecimal("100.00"),
                "Teste",
                999L
        );

        assertThrows(IllegalArgumentException.class, () -> processor.process(row));
    }

    @Test
    @DisplayName("Deve lançar exceção para descrição vazia")
    void deveLancarExcecaoParaDescricaoVazia() {
        ContaCsvRow row = new ContaCsvRow(
                LocalDate.of(2026, 3, 15),
                new BigDecimal("100.00"),
                "",
                1L
        );

        assertThrows(IllegalArgumentException.class, () -> processor.process(row));
    }

    @Test
    @DisplayName("Deve lançar exceção para valor nulo")
    void deveLancarExcecaoParaValorNulo() {
        ContaCsvRow row = new ContaCsvRow(
                LocalDate.of(2026, 3, 15),
                null,
                "Teste",
                1L
        );

        assertThrows(IllegalArgumentException.class, () -> processor.process(row));
    }

    @Test
    @DisplayName("Deve lançar exceção para data de vencimento nula")
    void deveLancarExcecaoParaDataNula() {
        ContaCsvRow row = new ContaCsvRow(
                null,
                new BigDecimal("100.00"),
                "Teste",
                1L
        );

        assertThrows(IllegalArgumentException.class, () -> processor.process(row));
    }

    @Test
    @DisplayName("Deve lançar exceção para fornecedorId nulo")
    void deveLancarExcecaoParaFornecedorIdNulo() {
        ContaCsvRow row = new ContaCsvRow(
                LocalDate.of(2026, 3, 15),
                new BigDecimal("100.00"),
                "Teste",
                null
        );

        assertThrows(IllegalArgumentException.class, () -> processor.process(row));
    }
}
