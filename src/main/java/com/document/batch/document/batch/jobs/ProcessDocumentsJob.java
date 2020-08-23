package com.document.batch.document.batch.jobs;

import com.document.batch.document.batch.tasklet.DocumentsProcessingTasklet;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;


@Configuration
@EnableBatchProcessing
public class ProcessDocumentsJob {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private SimpleJobLauncher jobLauncher;

    @Bean
    public ResourcelessTransactionManager springBatchTransactionManager() {
        return new ResourcelessTransactionManager();
    }

    @Bean
    public JobRepository jobRepository(ResourcelessTransactionManager springBatchTransactionManager) throws Exception {
        MapJobRepositoryFactoryBean mapJobRepositoryFactoryBean = new MapJobRepositoryFactoryBean(springBatchTransactionManager);
        mapJobRepositoryFactoryBean.setTransactionManager(springBatchTransactionManager);
        return mapJobRepositoryFactoryBean.getObject();
    }

    @Scheduled(cron = "*/5 * * * * *")
    public void perform() throws Exception {

        System.out.println("Job Started at :" + new Date());

        JobParameters param = new JobParametersBuilder().addString("JobID", String.valueOf(System.currentTimeMillis()))
                .toJobParameters();

        JobExecution execution = jobLauncher.run(readData(), param);

        System.out.println("Job finished with status :" + execution.getStatus());
    }

    @Bean
    public Job readData() {
        return jobBuilderFactory.get("readData").incrementer(new RunIdIncrementer()).flow(step1()).end().build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1").tasklet(documentsProcessingTasklet()).build();
    }

    @Bean
    public DocumentsProcessingTasklet documentsProcessingTasklet() {
        return new DocumentsProcessingTasklet();
    }

    @Bean
    public SimpleJobLauncher jobLauncher(JobRepository jobRepository) {
        SimpleJobLauncher launcher = new SimpleJobLauncher();
        launcher.setJobRepository(jobRepository);
        return launcher;
    }
}