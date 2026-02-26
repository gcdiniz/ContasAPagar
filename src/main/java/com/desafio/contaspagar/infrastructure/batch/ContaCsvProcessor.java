package com.desafio.contaspagar.infrastructure.batch;

import com.desafio.contaspagar.domain.entity.Conta;
import com.desafio.contaspagar.domain.entity.Fornecedor;
import com.desafio.contaspagar.infrastructure.repository.FornecedorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

/**
 * Processor do Spring Batch que converte um ContaCsvRow (linha do CSV)
 * em uma entidade Conta validada e pronta para persistência.
 */
public class ContaCsvProcessor implements ItemProcessor<ContaCsvRow, Conta> {

    private static final Logger log = LoggerFactory.getLogger(ContaCsvProcessor.class);

    private final FornecedorRepository fornecedorRepository;

    public ContaCsvProcessor(FornecedorRepository fornecedorRepository) {
        this.fornecedorRepository = fornecedorRepository;
    }

    @Override
    public Conta process(ContaCsvRow row) throws Exception {
        if (row.getDescricao() == null || row.getDescricao().isBlank()) {
            throw new IllegalArgumentException("Descrição não pode ser vazia");
        }

        if (row.getValor() == null) {
            throw new IllegalArgumentException("Valor é obrigatório");
        }

        if (row.getDataVencimento() == null) {
            throw new IllegalArgumentException("Data de vencimento é obrigatória");
        }

        if (row.getFornecedorId() == null) {
            throw new IllegalArgumentException("ID do fornecedor é obrigatório");
        }

        Fornecedor fornecedor = fornecedorRepository.findById(row.getFornecedorId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Fornecedor não encontrado com id: " + row.getFornecedorId()
                ));

        log.debug("Processando: {} | R$ {} | Fornecedor: {}",
                row.getDescricao(), row.getValor(), fornecedor.getNome());

        return new Conta(row.getDataVencimento(), row.getValor(), row.getDescricao(), fornecedor);
    }
}
