package com.desafio.contaspagar.application.service;

import com.desafio.contaspagar.application.dto.FornecedorRequest;
import com.desafio.contaspagar.application.dto.FornecedorResponse;
import com.desafio.contaspagar.domain.entity.Fornecedor;
import com.desafio.contaspagar.domain.exception.RecursoNaoEncontradoException;
import com.desafio.contaspagar.infrastructure.repository.ContaRepository;
import com.desafio.contaspagar.infrastructure.repository.FornecedorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;
    private final ContaRepository contaRepository;

    public FornecedorService(FornecedorRepository fornecedorRepository, ContaRepository contaRepository) {
        this.fornecedorRepository = fornecedorRepository;
        this.contaRepository = contaRepository;
    }

    @Transactional
    public FornecedorResponse criar(FornecedorRequest request) {
        Fornecedor fornecedor = new Fornecedor(request.nome());
        fornecedor = fornecedorRepository.save(fornecedor);
        return FornecedorResponse.fromEntity(fornecedor);
    }

    @Transactional(readOnly = true)
    public FornecedorResponse buscarPorId(Long id) {
        Fornecedor fornecedor = fornecedorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Fornecedor não encontrado com id: " + id));
        return FornecedorResponse.fromEntity(fornecedor);
    }

    @Transactional
    public FornecedorResponse atualizar(Long id, FornecedorRequest request) {
        Fornecedor fornecedor = fornecedorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Fornecedor não encontrado com id: " + id));
        fornecedor.setNome(request.nome());
        fornecedor = fornecedorRepository.save(fornecedor);
        return FornecedorResponse.fromEntity(fornecedor);
    }

    @Transactional
    public void excluir(Long id) {
        Fornecedor fornecedor = fornecedorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Fornecedor não encontrado com id: " + id));

        if (contaRepository.existsByFornecedorId(id)) {
            throw new IllegalStateException(
                    "Não é possível excluir o fornecedor '%s' pois existem contas vinculadas".formatted(fornecedor.getNome())
            );
        }

        fornecedorRepository.delete(fornecedor);
    }

    @Transactional(readOnly = true)
    public Page<FornecedorResponse> listar(Pageable pageable) {
        return fornecedorRepository.findAll(pageable).map(FornecedorResponse::fromEntity);
    }
}
