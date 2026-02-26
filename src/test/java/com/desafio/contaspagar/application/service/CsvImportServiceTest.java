package com.desafio.contaspagar.application.service;

import com.desafio.contaspagar.application.dto.CsvImportResponse;
import com.desafio.contaspagar.infrastructure.messaging.CsvImportPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CsvImportServiceTest {

    @Mock
    private CsvImportPublisher csvImportPublisher;

    @InjectMocks
    private CsvImportService csvImportService;

    @Test
    @DisplayName("Deve importar CSV com sucesso e retornar protocolo")
    void deveImportarCsvComSucesso() {
        String csvContent = "data_vencimento;valor;descricao;fornecedor_id\n2026-03-15;1500.00;Conta teste;1";
        MockMultipartFile file = new MockMultipartFile(
                "file", "contas.csv", "text/csv", csvContent.getBytes()
        );

        CsvImportResponse response = csvImportService.importarCsv(file);

        assertNotNull(response);
        assertNotNull(response.protocolo());
        assertFalse(response.protocolo().isEmpty());
        assertEquals("Arquivo recebido com sucesso. Processamento em andamento.", response.mensagem());
        verify(csvImportPublisher).publish(anyString(), anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção para arquivo nulo")
    void deveLancarExcecaoParaArquivoNulo() {
        assertThrows(IllegalArgumentException.class, () -> csvImportService.importarCsv(null));
        verify(csvImportPublisher, never()).publish(anyString(), anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção para arquivo vazio")
    void deveLancarExcecaoParaArquivoVazio() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "contas.csv", "text/csv", new byte[0]
        );

        assertThrows(IllegalArgumentException.class, () -> csvImportService.importarCsv(file));
        verify(csvImportPublisher, never()).publish(anyString(), anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção para arquivo sem extensão .csv")
    void deveLancarExcecaoParaArquivoSemExtensaoCsv() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "contas.txt", "text/plain", "conteudo".getBytes()
        );

        assertThrows(IllegalArgumentException.class, () -> csvImportService.importarCsv(file));
        verify(csvImportPublisher, never()).publish(anyString(), anyString());
    }
}
