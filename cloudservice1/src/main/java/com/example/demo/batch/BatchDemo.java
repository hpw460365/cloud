package com.example.demo.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@EnableBatchProcessing
@Configuration
public class BatchDemo {

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Bean
    public Job job(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        return jobBuilderFactory.get("chunk-time-job")
                .flow(step1(stepBuilderFactory))
                .next(step2(stepBuilderFactory))
                .next(step3(stepBuilderFactory))
                .build()
                .build();
    }


    public Step step1(StepBuilderFactory stepBuilderFactory){
        return stepBuilderFactory.get("tasklet1").tasklet(
                new Tasklet(){
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        stepContribution.setExitStatus(ExitStatus.FAILED);
                        return RepeatStatus.FINISHED;
                    }
                }
        ).allowStartIfComplete(true).build();
    }


    public Step step3(StepBuilderFactory stepBuilderFactory){
        return stepBuilderFactory.get("tasklet3").tasklet(
                new Tasklet(){
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        stepContribution.setExitStatus(ExitStatus.COMPLETED);
                        return RepeatStatus.FINISHED;
                    }
                }
        ).build();
    }



    @Resource(name="reader1") ItemReader<Long> reader;
    @Resource(name="writer1") ItemWriter<Long> writer;
    @Resource(name="processor1") ItemProcessor<Long, Long> processor;


    @Resource(name="reader1") ItemReader<SimpleBean> reader2;

    @Resource(name="writer2")
    FlatFileItemWriter<SimpleBean> writer2;
    @Resource(name="processor2") ItemProcessor<Long, SimpleBean> processor2;
    public Step step2(StepBuilderFactory stepBuilderFactory) {
        return stepBuilderFactory.get("chunk-step2")
                .<Long,Long>chunk(3)
                .reader(reader)
                .processor(processor)
                .writer(writer)
//                .faultTolerant()
//                .skip(RuntimeException.class).skipLimit(2)
//                .retry(RuntimeException.class).retryLimit(2)
//                .allowStartIfComplete(true)
//                .taskExecutor(taskExecutor())
                .build();
    }


    @StepScope
    @Bean("reader1")
    public ItemReader<Long> reader() throws InterruptedException {
        List<Long> longs = new ArrayList<>(1000);
        for (long i = 0; i < 10; i++) {
            longs.add(i);
//            if( i >= 16){
//                throw new RuntimeException();
//            }
        }
//        Thread.sleep(1000);
        logger.info("reader has exe, count={}", longs.size());
        return new ListItemReader<>(longs);
    }

    @StepScope
    @Bean("processor1")
    public  ItemProcessor<Long,Long> processor(){
        return new ItemProcessor<Long, Long>() {
            @Override
            public Long process(Long item) throws Exception {
                logger.info("process has exe, item={}", item);
//                if(item >= 3){
//                    throw new RuntimeException();
//                }
                return item;
            }
        };
    }

    @StepScope
    @Bean("writer1")
    public ItemWriter<Long> writer() {
        return new ItemWriter<Long>() {
            @Override
            public void write(List<? extends Long> items) throws Exception {
//                logger.info("writer has exe, count={}", items.size());
//                for(Long item: items){
//                    if(item == 3){
//                        throw new RuntimeException();
//                    }
//                }
//                for(Long item: items){
//                    logger.info("writer has exe, item={}", item);
//                }
            }
        };
    }








    @StepScope
    @Bean("processor2")
    public  ItemProcessor<Long,SimpleBean> processor2(){
        return new ItemProcessor<Long, SimpleBean>() {
            @Override
            public SimpleBean process(Long item) throws Exception {
                SimpleBean i = new SimpleBean();
                i.setNum(item);
                return i;
            }
        };
    }

    @Bean("writer2")
    @StepScope
    public FlatFileItemWriter<SimpleBean> flatFileItemWriter() throws Exception {
        FlatFileItemWriter<SimpleBean> writer = new FlatFileItemWriter<>();
        writer.setEncoding("UTF-8");
        DelimitedLineAggregator<SimpleBean> delimitedLineAggregator = new DelimitedLineAggregator<>();
        delimitedLineAggregator.setDelimiter("|");
        BeanWrapperFieldExtractor<SimpleBean> beanWrapperFieldExtractor = new BeanWrapperFieldExtractor();
        beanWrapperFieldExtractor.setNames(new String[]{"num"});
        delimitedLineAggregator.setFieldExtractor(beanWrapperFieldExtractor);
        writer.setLineAggregator(delimitedLineAggregator);
        FileSystemResource resource = new FileSystemResource("E:\\Temp\\hepeiwen\\temp.ok");
        writer.setResource(resource);
        writer.afterPropertiesSet();
        return writer;
    }


    class SimpleBean{
        Long num;

        public Long getNum() {
            return num;
        }

        public void setNum(Long num) {
            this.num = num;
        }
    }


    public ThreadPoolTaskExecutor taskExecutor(){
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(30);
        threadPoolTaskExecutor.setMaxPoolSize(3);
        threadPoolTaskExecutor.setThreadNamePrefix("executor-thread-");
        threadPoolTaskExecutor.setMaxPoolSize(100);
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }




}