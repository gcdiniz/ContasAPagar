package com.desafio.contaspagar.infrastructure.repository;

import com.desafio.contaspagar.domain.entity.Conta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContaRepository extends JpaRepository<Conta, UUID> {

    @Query("""
            SELECT c FROM Conta c
            JOIN FETCH c.fornecedor
            WHERE c.id = :id
            """)
    Optional<Conta> findByIdWithFornecedor(@Param("id") UUID id);

    @Query(value = """
            SELECT c FROM Conta c
            JOIN FETCH c.fornecedor
            WHERE (:dataVencimento IS NULL OR c.dataVencimento = :dataVencimento)
              AND (:descricao IS NULL OR LOWER(CAST(c.descricao AS String)) LIKE LOWER(CONCAT('%', CAST(:descricao AS String), '%')))
            """,
            countQuery = """
                    SELECT COUNT(c) FROM Conta c
                    WHERE (:dataVencimento IS NULL OR c.dataVencimento = :dataVencimento)
                      AND (:descricao IS NULL OR LOWER(CAST(c.descricao AS String)) LIKE LOWER(CONCAT('%', CAST(:descricao AS String), '%')))
                    """)
    Page<Conta> findAllWithFilters(
            @Param("dataVencimento") LocalDate dataVencimento,
            @Param("descricao") String descricao,
            Pageable pageable
    );

    @Query("""
            SELECT COALESCE(SUM(c.valor), 0)
            FROM Conta c
            WHERE c.situacao = 2
              AND c.dataPagamento >= :dataInicio
              AND c.dataPagamento <= :dataFim
            """)
    BigDecimal sumValorPagoByPeriodo(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim
    );

    boolean existsByFornecedorId(Long fornecedorId);
}
