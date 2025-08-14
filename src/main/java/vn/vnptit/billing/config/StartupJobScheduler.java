package vn.vnptit.billing.config;

import vn.vnptit.billing.service.ManualJobService;
import org.jobrunr.scheduling.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StartupJobScheduler {
    private static final Logger logger = LoggerFactory.getLogger(StartupJobScheduler.class);

    @Bean
    public ApplicationRunner scheduleStartupJobs(@Autowired(required = false) JobScheduler jobScheduler, ManualJobService jobService) {
        return args -> {
            if (jobScheduler == null) {
                logger.error("JobScheduler bean is not available! Check your configuration.");
                logger.info("Available beans in application context:");
                return;
            }

            logger.info("JobScheduler found: {}", jobScheduler.getClass().getSimpleName());
            logger.info("Scheduling startup jobs...");

            try {
                // Schedule some sample jobs on startup
                var jobId = jobScheduler.enqueue(() -> jobService.executeSimpleJob("Welcome to JobRunr!"));
                logger.info("Startup job scheduled with ID: {}", jobId);

                logger.info("Startup jobs scheduled successfully");
                logger.info("JobRunr Dashboard available at: http://localhost:8081");
                logger.info("API endpoints available at: http://localhost:8082/api/jobs/status");
            } catch (Exception e) {
                logger.error("Failed to schedule startup jobs", e);
            }
        };
    }
}