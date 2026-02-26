package com.desafio.contaspagar.application.service;

import com.desafio.contaspagar.application.dto.AlterarSituacaoRequest;
import com.desafio.contaspagar.application.dto.ContaRequest;
import com.desafio.contaspagar.application.dto.ContaResponse;
import com.desafio.contaspagar.application.dto.TotalPagoPeriodoResponse;
import com.desafio.contaspagar.domain.entity.Conta;
import com.desafio.contaspagar.domain.entity.Fornecedor;
import com.desafio.contaspagar.domain.enums.SituacaoConta;
import com.desafio.contaspagar.domain.exception.RecursoNaoEncontradoException;
import com.desafio.contaspagar.domain.exception.TransicaoSituacaoInvalidaException;
import com.desafio.contaspagar.infrastructure.repository.ContaRepository;
import com.desafio.contaspagar.infrastructure.repository.FornecedorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContaServiceTest {

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private FornecedorRepository fornecedorRepository;

    @InjectMocks
    private ContaService contaService;

    private Fornecedor fornecedor;
    private Conta conta;

    @BeforeEach
    void setUp() {
        fornecedor = new Fornecedor("Fornecedor Teste");
        conta = new Conta(
                LocalDate.of(2026, 3, 15),
                new BigDecimal("1500.00"),
                "Conta de teste",
                fornecedor
        );
    }

    // --- Criar Conta ---

    @Test
    @DisplayName("Deve criar conta com sucesso")
    void deveCriarContaComSucesso() {
        ContaRequest request = new ContaRequest(
                LocalDate.of(2026, 3, 15),
                new BigDecimal("1500.00"),
                "Conta de teste",
                1L
        );

        when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedor));
        when(contaRepository.save(any(Conta.class))).thenReturn(conta);

        ContaResponse response = contaService.criar(request);

        assertNotNull(response);
        assertEquals("Conta de teste", response.descricao());
        assertEquals(new BigDecimal("1500.00"), response.valor());
        assertEquals(SituacaoConta.PENDENTE, response.situacao());
        verify(contaRepository).save(any(Conta.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando fornecedor não encontrado ao criar")
    void deveLancarExcecaoQuandoFornecedorNaoEncontrado() {
        ContaRequest request = new ContaRequest(
                LocalDate.of(2026, 3, 15),
                new BigDecimal("1500.00"),
                "Conta de teste",
                999L
        );

        when(fornecedorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> contaService.criar(request));
        verify(contaRepository, never()).save(any());
    }

    // --- Buscar Conta ---

    @Test
    @DisplayName("Deve buscar conta por ID com sucesso")
    void deveBuscarContaPorIdComSucesso() {
        UUID id = UUID.randomUUID();
        when(contaRepository.findByIdWithFornecedor(id)).thenReturn(Optional.of(conta));

        ContaResponse response = contaService.buscarPorId(id);

        assertNotNull(response);
        assertEquals("Conta de teste", response.descricao());
    }

    @Test
    @DisplayName("Deve lançar exceção quando conta não encontrada")
    void deveLancarExcecaoQuandoContaNaoEncontrada() {
        UUID id = UUID.randomUUID();
        when(contaRepository.findByIdWithFornecedor(id)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> contaService.buscarPorId(id));
    }

    // --- Alterar Situação ---

    @Test
    @DisplayName("Deve alterar situação para PAGO com sucesso")
    void deveAlterarSituacaoParaPagoComSucesso() {
        UUID id = UUID.randomUUID();
        AlterarSituacaoRequest request = new AlterarSituacaoRequest(SituacaoConta.PAGO);

        when(contaRepository.findByIdWithFornecedor(id)).thenReturn(Optional.of(conta));
        when(contaRepository.save(any(Conta.class))).thenReturn(conta);

        ContaResponse response = contaService.alterarSituacao(id, request);

        assertNotNull(response);
        assertEquals(SituacaoConta.PAGO, response.situacao());
    }

    @Test
    @DisplayName("Deve lançar exceção para transição inválida")
    void deveLancarExcecaoParaTransicaoInvalida() {
        UUID id = UUID.randomUUID();
        conta.alterarSituacao(SituacaoConta.PAGO);
        AlterarSituacaoRequest request = new AlterarSituacaoRequest(SituacaoConta.PENDENTE);

        when(contaRepository.findByIdWithFornecedor(id)).thenReturn(Optional.of(conta));

        assertThrows(TransicaoSituacaoInvalidaException.class, () ->
                contaService.alterarSituacao(id, request)
        );
    }

    // --- Listar Contas ---

    @Test
    @DisplayName("Deve listar contas paginadas")
    void deveListarContasPaginadas() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Conta> page = new PageImpl<>(List.of(conta));

        when(contaRepository.findAllWithFilters(any(), any(), eq(pageable))).thenReturn(page);

        Page<ContaResponse> result = contaService.listar(null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    // --- Total Pago por Período ---

    @Test
    @DisplayName("Deve retornar total pago no período")
    void deveRetornarTotalPagoNoPeriodo() {
        LocalDate inicio = LocalDate.of(2026, 1, 1);
        LocalDate fim = LocalDate.of(2026, 12, 31);

        when(contaRepository.sumValorPagoByPeriodo(inicio, fim))
                .thenReturn(new BigDecimal("5000.00"));

        TotalPagoPeriodoResponse response = contaService.totalPagoPorPeriodo(inicio, fim);

        assertEquals(new BigDecimal("5000.00"), response.totalPago());
        assertEquals(inicio, response.dataInicio());
        assertEquals(fim, response.dataFim());
    }

    @Test
    @DisplayName("Deve retornar zero quando não há pagamentos no período")
    void deveRetornarZeroQuandoNaoHaPagamentos() {
        LocalDate inicio = LocalDate.of(2026, 1, 1);
        LocalDate fim = LocalDate.of(2026, 12, 31);

        when(contaRepository.sumValorPagoByPeriodo(inicio, fim)).thenReturn(null);

        TotalPagoPeriodoResponse response = contaService.totalPagoPorPeriodo(inicio, fim);

        assertEquals(BigDecimal.ZERO, response.totalPago());
    }

    @Test
    @DisplayName("Deve lançar exceção quando dataInicio é posterior a dataFim")
    void deveLancarExcecaoQuandoDataInicioMaiorQueDataFim() {
        LocalDate inicio = LocalDate.of(2026, 12, 31);
        LocalDate fim = LocalDate.of(2026, 1, 1);

        assertThrows(IllegalArgumentException.class, () ->
                contaService.totalPagoPorPeriodo(inicio, fim)
        );
        verify(contaRepository, never()).sumValorPagoByPeriodo(any(), any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando datas são nulas")
    void deveLancarExcecaoQuandoDatasNulas() {
        assertThrows(IllegalArgumentException.class, () ->
                contaService.totalPagoPorPeriodo(null, LocalDate.now())
        );
        assertThrows(IllegalArgumentException.class, () ->
                contaService.totalPagoPorPeriodo(LocalDate.now(), null)
        );
    }
}
