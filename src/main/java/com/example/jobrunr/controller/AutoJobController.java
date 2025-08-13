package vn.vnpt.billing.controller;

import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.scheduling.cron.Cron;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.vnpt.billing.service.JobService;

import java.time.ZoneId;

@Configuration
public class AutoJobController {

    @Autowired
    private JobScheduler jobScheduler;

    @Autowired
    private JobService jobService;

    @Bean
    CommandLineRunner exportData(JobScheduler jobScheduler) {
        return args -> {
            var jobId = jobScheduler.scheduleRecurrently(
                    "export-data-job",
                    Cron.minutely(),
                    ZoneId.systemDefault(),
                    () -> jobService.executeSimpleJob("Export data")
            );
            System.out.println("exportData - jobId: " + jobId);
        };
    }

}
