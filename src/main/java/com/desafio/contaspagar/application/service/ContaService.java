package com.desafio.contaspagar.application.service;

import com.desafio.contaspagar.application.dto.AlterarSituacaoRequest;
import com.desafio.contaspagar.application.dto.ContaRequest;
import com.desafio.contaspagar.application.dto.ContaResponse;
import com.desafio.contaspagar.application.dto.TotalPagoPeriodoResponse;
import com.desafio.contaspagar.application.dto.*;
import com.desafio.contaspagar.domain.entity.Conta;
import com.desafio.contaspagar.domain.entity.Fornecedor;
import com.desafio.contaspagar.domain.exception.RecursoNaoEncontradoException;
import com.desafio.contaspagar.infrastructure.repository.ContaRepository;
import com.desafio.contaspagar.infrastructure.repository.FornecedorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class ContaService {

    private final ContaRepository contaRepository;
    private final FornecedorRepository fornecedorRepository;

    public ContaService(ContaRepository contaRepository, FornecedorRepository fornecedorRepository) {
        this.contaRepository = contaRepository;
        this.fornecedorRepository = fornecedorRepository;
    }

    @Transactional
    public ContaResponse criar(ContaRequest request) {
        Fornecedor fornecedor = buscarFornecedor(request.fornecedorId());
        Conta conta = new Conta(
                request.dataVencimento(),
                request.valor(),
                request.descricao(),
                fornecedor
        );
        conta = contaRepository.save(conta);
        return ContaResponse.fromEntity(conta);
    }

    @Transactional(readOnly = true)
    public ContaResponse buscarPorId(UUID id) {
        Conta conta = buscarConta(id);
        return ContaResponse.fromEntity(conta);
    }

    @Transactional
    public ContaResponse atualizar(UUID id, ContaRequest request) {
        Conta conta = buscarConta(id);
        Fornecedor fornecedor = buscarFornecedor(request.fornecedorId());
        conta.atualizar(request.dataVencimento(), request.valor(), request.descricao(), fornecedor);
        conta = contaRepository.save(conta);
        return ContaResponse.fromEntity(conta);
    }

    @Transactional
    public ContaResponse alterarSituacao(UUID id, AlterarSituacaoRequest request) {
        Conta conta = buscarConta(id);
        conta.alterarSituacao(request.situacao());
        conta = contaRepository.save(conta);
        return ContaResponse.fromEntity(conta);
    }

    @Transactional
    public void excluir(UUID id) {
        Conta conta = buscarConta(id);
        contaRepository.delete(conta);
    }

    @Transactional(readOnly = true)
    public Page<ContaResponse> listar(LocalDate dataVencimento, String descricao, Pageable pageable) {
        Page<Conta> contas = contaRepository.findAllWithFilters(
                dataVencimento,
                (descricao != null && !descricao.isBlank()) ? descricao : null,
                pageable
        );
        return contas.map(ContaResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public TotalPagoPeriodoResponse totalPagoPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("Data de início e data de fim são obrigatórias");
        }
        if (dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("Data de início não pode ser posterior à data de fim");
        }
        BigDecimal total = contaRepository.sumValorPagoByPeriodo(dataInicio, dataFim);
        return new TotalPagoPeriodoResponse(dataInicio, dataFim, total != null ? total : BigDecimal.ZERO);
    }

    private Conta buscarConta(UUID id) {
        return contaRepository.findByIdWithFornecedor(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada com id: " + id));
    }

    private Fornecedor buscarFornecedor(Long id) {
        return fornecedorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Fornecedor não encontrado com id: " + id));
    }
}
