package com.desafio.contaspagar.domain.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fornecedor")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Fornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    public Fornecedor(String nome) {
        setNome(nome);
    }

    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do fornecedor não pode ser vazio");
        }
        if (nome.trim().length() > 255) {
            throw new IllegalArgumentException("Nome do fornecedor deve ter no máximo 255 caracteres");
        }
        this.nome = nome.trim();
    }
}
