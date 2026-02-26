package com.desafio.contaspagar.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FornecedorTest {

    @Test
    @DisplayName("Deve criar fornecedor com nome válido")
    void deveCriarFornecedorComNomeValido() {
        Fornecedor fornecedor = new Fornecedor("Fornecedor Teste");

        assertEquals("Fornecedor Teste", fornecedor.getNome());
    }

    @Test
    @DisplayName("Deve fazer trim do nome")
    void deveFazerTrimDoNome() {
        Fornecedor fornecedor = new Fornecedor("  Fornecedor Teste  ");

        assertEquals("Fornecedor Teste", fornecedor.getNome());
    }

    @Test
    @DisplayName("Deve lançar exceção para nome nulo")
    void deveLancarExcecaoParaNomeNulo() {
        assertThrows(IllegalArgumentException.class, () -> new Fornecedor(null));
    }

    @Test
    @DisplayName("Deve lançar exceção para nome vazio")
    void deveLancarExcecaoParaNomeVazio() {
        assertThrows(IllegalArgumentException.class, () -> new Fornecedor(""));
    }

    @Test
    @DisplayName("Deve lançar exceção para nome em branco")
    void deveLancarExcecaoParaNomeEmBranco() {
        assertThrows(IllegalArgumentException.class, () -> new Fornecedor("   "));
    }

    @Test
    @DisplayName("Deve atualizar nome do fornecedor")
    void deveAtualizarNomeFornecedor() {
        Fornecedor fornecedor = new Fornecedor("Nome Original");
        fornecedor.setNome("Nome Atualizado");

        assertEquals("Nome Atualizado", fornecedor.getNome());
    }

    @Test
    @DisplayName("Deve lançar exceção para nome com mais de 255 caracteres")
    void deveLancarExcecaoParaNomeMuitoLongo() {
        String nomeLongo = "A".repeat(256);
        assertThrows(IllegalArgumentException.class, () -> new Fornecedor(nomeLongo));
    }
}
