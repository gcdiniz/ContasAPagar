package com.desafio.contaspagar.application.service;

import com.desafio.contaspagar.application.dto.CsvImportResponse;
import com.desafio.contaspagar.infrastructure.messaging.CsvImportPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class CsvImportService {

    private final CsvImportPublisher csvImportPublisher;

    public CsvImportService(CsvImportPublisher csvImportPublisher) {
        this.csvImportPublisher = csvImportPublisher;
    }

    public CsvImportResponse importarCsv(MultipartFile file) {
        validarArquivo(file);

        String protocolo = UUID.randomUUID().toString();

        try {
            String csvContent = new String(file.getBytes());
            csvImportPublisher.publish(protocolo, csvContent);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler o arquivo CSV", e);
        }

        return new CsvImportResponse(
                protocolo,
                "Arquivo recebido com sucesso. Processamento em andamento."
        );
    }

    private void validarArquivo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo CSV é obrigatório");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("O arquivo deve ter extensão .csv");
        }
    }
}
