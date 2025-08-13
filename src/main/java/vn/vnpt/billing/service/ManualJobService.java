package vn.vnpt.billing.service;

import org.jobrunr.jobs.annotations.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ManualJobService {
    private static final Logger logger = LoggerFactory.getLogger(ManualJobService.class);

    @Job(name = "Simple Background Job", retries = 2)
    public void executeSimpleJob(String message) {
        logger.info("Executing simple job with message: {}", message);

        try {
            // Simulate some work
            Thread.sleep(2000);

            logger.info("Simple job completed successfully at {}", LocalDateTime.now());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Job was interrupted", e);
            throw new RuntimeException("Job execution failed", e);
        }
    }

    @Job(name = "Email Sending Job", retries = 3)
    public void sendEmail(String recipient, String subject, String content) {
        logger.info("Sending email to: {} with subject: {}", recipient, subject);

        try {
            // Simulate email sending
            Thread.sleep(1000);

            // Simulate occasional failure for testing
            if (Math.random() < 0.2) {
                throw new RuntimeException("Email service temporarily unavailable");
            }

            logger.info("Email sent successfully to {} at {}", recipient, LocalDateTime.now());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Email sending was interrupted", e);
            throw new RuntimeException("Email sending failed", e);
        }
    }

    @Job(name = "Data Processing Job", retries = 1)
    public void processData(String dataId, int processingType) {
        logger.info("Processing data with ID: {}, type: {}", dataId, processingType);

        try {
            // Simulate data processing
            Thread.sleep(3000);

            switch (processingType) {
                case 1:
                    logger.info("Performing basic data processing for {}", dataId);
                    break;
                case 2:
                    logger.info("Performing advanced data processing for {}", dataId);
                    break;
                default:
                    logger.info("Performing default data processing for {}", dataId);
            }

            logger.info("Data processing completed for {} at {}", dataId, LocalDateTime.now());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Data processing was interrupted", e);
            throw new RuntimeException("Data processing failed", e);
        }
    }

    @Job(name = "Long Running Job", retries = 0)
    public void longRunningJob(String taskName) {
        logger.info("Starting long running job: {}", taskName);

        try {
            for (int i = 1; i <= 10; i++) {
                logger.info("Long running job {} - Step {}/10", taskName, i);
                Thread.sleep(2000);
            }

            logger.info("Long running job {} completed at {}", taskName, LocalDateTime.now());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Long running job was interrupted", e);
            throw new RuntimeException("Long running job failed", e);
        }
    }
}