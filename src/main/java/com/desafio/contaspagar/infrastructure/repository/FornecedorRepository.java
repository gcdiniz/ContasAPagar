package com.desafio.contaspagar.infrastructure.repository;

import com.desafio.contaspagar.domain.entity.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {
}
