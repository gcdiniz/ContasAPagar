package com.desafio.contaspagar.infrastructure.batch;

import com.desafio.contaspagar.domain.entity.Conta;
import com.desafio.contaspagar.infrastructure.repository.ContaRepository;
import com.desafio.contaspagar.infrastructure.repository.FornecedorRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Configuração do Spring Batch para importação de contas via CSV.
 *
 *   Reader: FlatFileItemReader (lê CSV linha a linha -> ContaCsvRow)
 *   Processor: ContaCsvProcessor (valida e converte ContaCsvRow -> Conta)
 *   Writer: RepositoryItemWriter (persiste Conta via JPA)
 *
 * Chunk size: 100 (commit a cada 100 registros)
 * Skip policy: pula até 1000 linhas com erro sem abortar o Job
 */
@Configuration
public class CsvImportBatchConfig {

    @Value("${app.batch.chunk-size:100}")
    private int chunkSize;

    @Value("${app.batch.skip-limit:1000}")
    private int skipLimit;

    @Bean
    public ConversionService batchConversionService() {
        DefaultConversionService conversionService = new DefaultConversionService();

        conversionService.addConverter(new Converter<String, LocalDate>() {
            @Override
            public LocalDate convert(String source) {
                return LocalDate.parse(source.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            }
        });

        conversionService.addConverter(new Converter<String, BigDecimal>() {
            @Override
            public BigDecimal convert(String source) {
                return new BigDecimal(source.trim());
            }
        });

        conversionService.addConverter(new Converter<String, Long>() {
            @Override
            public Long convert(String source) {
                return Long.parseLong(source.trim());
            }
        });

        return conversionService;
    }

    /**
     * Cria um FlatFileItemReader para um conteúdo CSV específico.
     */
    public FlatFileItemReader<ContaCsvRow> csvReader(String csvContent) {
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(";");
        tokenizer.setNames("dataVencimento", "valor", "descricao", "fornecedorId");

        BeanWrapperFieldSetMapper<ContaCsvRow> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(ContaCsvRow.class);
        fieldSetMapper.setConversionService(batchConversionService());

        DefaultLineMapper<ContaCsvRow> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        FlatFileItemReader<ContaCsvRow> reader = new FlatFileItemReader<>();
        reader.setResource(new ByteArrayResource(csvContent.getBytes()));
        reader.setLinesToSkip(1); // pular cabeçalho
        reader.setLineMapper(lineMapper);
        reader.setStrict(false);

        return reader;
    }

    @Bean
    public ContaCsvProcessor contaCsvProcessor(FornecedorRepository fornecedorRepository) {
        return new ContaCsvProcessor(fornecedorRepository);
    }

    @Bean
    public RepositoryItemWriter<Conta> contaWriter(ContaRepository contaRepository) {
        return new RepositoryItemWriterBuilder<Conta>()
                .repository(contaRepository)
                .methodName("save")
                .build();
    }

    @Bean
    public Job csvImportJob(JobRepository jobRepository, Step csvImportStep) {
        return new JobBuilder("csvImportJob", jobRepository)
                .listener(new CsvJobCompletionListener())
                .start(csvImportStep)
                .build();
    }

    @Bean
    public Step csvImportStep(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager,
                              ContaCsvProcessor contaCsvProcessor,
                              RepositoryItemWriter<Conta> contaWriter) {
        return new StepBuilder("csvImportStep", jobRepository)
                .<ContaCsvRow, Conta>chunk(chunkSize, transactionManager)
                .reader(csvReader(""))
                .processor(contaCsvProcessor)
                .writer(contaWriter)
                .faultTolerant()
                .skipLimit(skipLimit)
                .skip(FlatFileParseException.class)
                .skip(IllegalArgumentException.class)
                .skip(Exception.class)
                .noRetry(Exception.class)
                .listener(new CsvSkipListener(""))
                .build();
    }
}
