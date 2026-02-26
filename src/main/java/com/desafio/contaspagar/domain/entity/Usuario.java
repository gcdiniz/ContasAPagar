package com.desafio.contaspagar.domain.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuario")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nm_usuario", nullable = false, unique = true, length = 100)
    private String usuario;

    @Column(name = "senha", nullable = false)
    private String senha;

    @Column(nullable = false, length = 50)
    private String role;

    public Usuario(String usuario, String senha, String role) {
        this.usuario = usuario;
        this.senha = senha;
        this.role = role;
    }
}
