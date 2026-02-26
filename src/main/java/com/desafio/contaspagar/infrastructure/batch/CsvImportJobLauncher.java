package com.desafio.contaspagar.infrastructure.batch;

import com.desafio.contaspagar.domain.entity.Conta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Serviço responsável por criar e executar dinamicamente um Job do Spring Batch
 * para cada importação de CSV recebida via RabbitMQ.
 *
 * Cada execução cria um Job com Reader configurado para o conteúdo CSV específico,
 * garantindo isolamento entre importações concorrentes.
 */
@Service
public class CsvImportJobLauncher {

    private static final Logger log = LoggerFactory.getLogger(CsvImportJobLauncher.class);

    private final JobLauncher jobLauncher;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final CsvImportBatchConfig batchConfig;
    private final ContaCsvProcessor contaCsvProcessor;
    private final RepositoryItemWriter<Conta> contaWriter;

    public CsvImportJobLauncher(JobLauncher jobLauncher,
                                 JobRepository jobRepository,
                                 PlatformTransactionManager transactionManager,
                                 CsvImportBatchConfig batchConfig,
                                 ContaCsvProcessor contaCsvProcessor,
                                 RepositoryItemWriter<Conta> contaWriter) {
        this.jobLauncher = jobLauncher;
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.batchConfig = batchConfig;
        this.contaCsvProcessor = contaCsvProcessor;
        this.contaWriter = contaWriter;
    }

    /**
     * Executa o Job de importação CSV com o conteúdo e protocolo fornecidos.
     * Cria dinamicamente o Reader e o SkipListener para cada execução.
     */
    public void executar(String protocolo, String csvContent) {
        try {
            Step step = new StepBuilder("csvImportStep", jobRepository)
                    .<ContaCsvRow, Conta>chunk(100, transactionManager)
                    .reader(batchConfig.csvReader(csvContent))
                    .processor(contaCsvProcessor)
                    .writer(contaWriter)
                    .faultTolerant()
                    .skipLimit(1000)
                    .skip(FlatFileParseException.class)
                    .skip(IllegalArgumentException.class)
                    .skip(Exception.class)
                    .noRetry(Exception.class)
                    .listener(new CsvSkipListener(protocolo))
                    .build();

            Job job = new JobBuilder("csvImportJob-" + protocolo, jobRepository)
                    .listener(new CsvJobCompletionListener())
                    .start(step)
                    .build();

            JobParameters params = new JobParametersBuilder()
                    .addString("protocolo", protocolo)
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(job, params);

            log.info("Job executado - Protocolo: {} | Status: {}", protocolo, execution.getStatus());

        } catch (Exception e) {
            log.error("Erro ao executar Job de importação - Protocolo: {}", protocolo, e);
        }
    }
}
