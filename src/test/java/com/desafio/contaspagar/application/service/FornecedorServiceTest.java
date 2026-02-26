package com.desafio.contaspagar.application.service;

import com.desafio.contaspagar.application.dto.FornecedorRequest;
import com.desafio.contaspagar.application.dto.FornecedorResponse;
import com.desafio.contaspagar.domain.entity.Fornecedor;
import com.desafio.contaspagar.domain.exception.RecursoNaoEncontradoException;
import com.desafio.contaspagar.infrastructure.repository.ContaRepository;
import com.desafio.contaspagar.infrastructure.repository.FornecedorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FornecedorServiceTest {

    @Mock
    private FornecedorRepository fornecedorRepository;

    @Mock
    private ContaRepository contaRepository;

    @InjectMocks
    private FornecedorService fornecedorService;

    @Test
    @DisplayName("Deve criar fornecedor com sucesso")
    void deveCriarFornecedorComSucesso() {
        FornecedorRequest request = new FornecedorRequest("Novo Fornecedor");
        Fornecedor fornecedor = new Fornecedor("Novo Fornecedor");

        when(fornecedorRepository.save(any(Fornecedor.class))).thenReturn(fornecedor);

        FornecedorResponse response = fornecedorService.criar(request);

        assertNotNull(response);
        assertEquals("Novo Fornecedor", response.nome());
        verify(fornecedorRepository).save(any(Fornecedor.class));
    }

    @Test
    @DisplayName("Deve buscar fornecedor por ID com sucesso")
    void deveBuscarFornecedorPorIdComSucesso() {
        Fornecedor fornecedor = new Fornecedor("Fornecedor Teste");
        when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedor));

        FornecedorResponse response = fornecedorService.buscarPorId(1L);

        assertNotNull(response);
        assertEquals("Fornecedor Teste", response.nome());
    }

    @Test
    @DisplayName("Deve lançar exceção quando fornecedor não encontrado")
    void deveLancarExcecaoQuandoFornecedorNaoEncontrado() {
        when(fornecedorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () ->
                fornecedorService.buscarPorId(999L)
        );
    }

    @Test
    @DisplayName("Deve atualizar fornecedor com sucesso")
    void deveAtualizarFornecedorComSucesso() {
        Fornecedor fornecedor = new Fornecedor("Nome Original");
        FornecedorRequest request = new FornecedorRequest("Nome Atualizado");

        when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedor));
        when(fornecedorRepository.save(any(Fornecedor.class))).thenReturn(fornecedor);

        FornecedorResponse response = fornecedorService.atualizar(1L, request);

        assertNotNull(response);
        assertEquals("Nome Atualizado", response.nome());
    }

    @Test
    @DisplayName("Deve excluir fornecedor com sucesso")
    void deveExcluirFornecedorComSucesso() {
        Fornecedor fornecedor = new Fornecedor("Fornecedor Teste");
        when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedor));
        when(contaRepository.existsByFornecedorId(1L)).thenReturn(false);

        assertDoesNotThrow(() -> fornecedorService.excluir(1L));
        verify(fornecedorRepository).delete(fornecedor);
    }

    @Test
    @DisplayName("Deve lançar exceção ao excluir fornecedor com contas vinculadas")
    void deveLancarExcecaoAoExcluirFornecedorComContas() {
        Fornecedor fornecedor = new Fornecedor("Fornecedor Teste");
        when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedor));
        when(contaRepository.existsByFornecedorId(1L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> fornecedorService.excluir(1L));
        verify(fornecedorRepository, never()).delete(any());
    }
}
