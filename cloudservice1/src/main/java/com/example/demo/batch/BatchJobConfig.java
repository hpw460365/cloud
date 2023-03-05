package com.example.demo.batch;

import com.example.demo.bean.SimpleBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EnableBatchProcessing
@Configuration
public class BatchJobConfig {

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
                        chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put("param", chunkContext.hashCode());
//                        stepContribution.setExitStatus(ExitStatus.FAILED);
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
                        logger.info("step3 get exe param:{}", chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().get("param"));
                        stepContribution.setExitStatus(ExitStatus.COMPLETED);
                        return RepeatStatus.FINISHED;
                    }
                }
        ).build();
    }



    @Resource(name="reader1") ItemReader<Long> reader;
    @Resource(name="writer1") ItemWriter<Long> writer;
    @Resource(name="processor1") ItemProcessor<Long, Long> processor;
    @Resource(name="reader2") ItemReader<SimpleBean> reader2;
    @Resource(name="writer2")
    FlatFileItemWriter<SimpleBean> writer2;
    @Resource(name="processor2") ItemProcessor<SimpleBean, SimpleBean> processor2;
    public Step step2(StepBuilderFactory stepBuilderFactory) {
        return stepBuilderFactory.get("chunk-step2")
                .<SimpleBean,SimpleBean>chunk(3)
                .reader(reader2)
                .processor(processor2)
                .writer(writer2)
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
        List<Long> longs = new ArrayList<>(20);
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
                logger.info("writer has exe, count={}", items.size());
                for(Long item: items){
                    if(item >= 3){
                        throw new RuntimeException();
                    }
                }
//                for(Long item: items){
//                    logger.info("writer has exe, item={}", item);
//                }
            }
        };
    }







    @Resource(name = "d2DataSource")
    private DataSource d2DataSource;
    @StepScope
    @Bean("reader2")
    public ItemReader<SimpleBean> reader2(@Value("#{jobExecutionContext['param']}") String param) throws InterruptedException {

        logger.info("reader get the  param: {}", param);
        Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put("id", Order.ASCENDING);
        return new JdbcPagingItemReaderBuilder<SimpleBean>().dataSource(d2DataSource)
                .fetchSize(20)
                .pageSize(10)
                .selectClause("select id, username, password, password_salt")
                .fromClause("from users")
                .sortKeys(sortKeys)
                .maxItemCount(20)
                .rowMapper(new BeanPropertyRowMapper<SimpleBean>(SimpleBean.class))
                .name("pagingReader2")
                .build();
    }




    @StepScope
    @Bean("processor2")
    public  ItemProcessor<SimpleBean,SimpleBean> processor2(){
        return new ItemProcessor<SimpleBean, SimpleBean>() {
            @Override
            public SimpleBean process(SimpleBean item) throws Exception {
//                logger.info("process has exe, item.id={}", item.getId());
                item.setNum(Long.valueOf(1));
                return item;
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
        beanWrapperFieldExtractor.setNames(new String[]{"id", "username", "password", "password_salt", "num"});
        delimitedLineAggregator.setFieldExtractor(beanWrapperFieldExtractor);
        writer.setLineAggregator(delimitedLineAggregator);
        FileSystemResource resource = new FileSystemResource("E:\\Temp\\hepeiwen\\temp.ok");
        writer.setResource(resource);
        writer.afterPropertiesSet();
        return writer;
    }




    public ThreadPoolTaskExecutor taskExecutor(){
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(3);
        threadPoolTaskExecutor.setMaxPoolSize(3);
        threadPoolTaskExecutor.setThreadNamePrefix("executor-thread-");
        threadPoolTaskExecutor.setMaxPoolSize(100);
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }




}