package vn.vnpt.billing.controller;

import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.scheduling.cron.Cron;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.vnpt.billing.service.ExportDataService;
import vn.vnpt.billing.service.UploadFTPService;

import java.time.ZoneId;

@Configuration
public class ScheduleJobController {

    @Autowired
    private JobScheduler jobScheduler;

    @Autowired
    private ExportDataService exportDataService;

    @Autowired
    private UploadFTPService uploadFTPService;

    @Bean
    CommandLineRunner exportData(JobScheduler jobScheduler) {
        return args -> {
            jobScheduler.scheduleRecurrently(
                    "export-data-job",
                    Cron.monthly(7, 1), // ngày 7, giờ 1h
                    ZoneId.of("Asia/Ho_Chi_Minh"),
                    () -> exportDataService.run(null)
            );
        };
    }

    @Bean
    CommandLineRunner uploadFTP(JobScheduler jobScheduler) {
        return args -> {
            jobScheduler.scheduleRecurrently(
                    "upload-ftp-job",
                    Cron.monthly(8, 1), // ngày 8, giờ 1h
                    ZoneId.of("Asia/Ho_Chi_Minh"),
                    () -> uploadFTPService.run(null)
            );
        };
    }

}
