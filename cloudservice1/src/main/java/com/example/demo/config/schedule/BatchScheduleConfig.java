package com.example.demo.config.schedule;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Configuration
@EnableScheduling
@EnableAsync
public class BatchScheduleConfig {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job job;

    //    @Scheduled(cron= "0 0/1 * * * ?")
//    @Async
    public void startBatchDemo() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        JobParameters p = new JobParametersBuilder().addDate("date", new Date()).toJobParameters();
        jobLauncher.run(job, p);
    }

    //    @Scheduled(cron= "0 0/1 * * * ?")
    public void testAsync() {
        String fileDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        logger.info("async thread started succeed, {}", fileDate);
    }
}
