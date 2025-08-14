package vn.vnptit.billing.controller;

import org.jobrunr.jobs.JobId;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.scheduling.cron.Cron;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.vnptit.billing.service.ManualJobService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
public class ManualJobController {

    @Autowired
    private JobScheduler jobScheduler;

    @Autowired
    private ManualJobService manualJobService;

    @PostMapping("/simple")
    public ResponseEntity<Map<String, Object>> scheduleSimpleJob(@RequestParam String message) {
        var jobId = jobScheduler.enqueue(() -> manualJobService.executeSimpleJob(message));

        Map<String, Object> response = new HashMap<>();
        response.put("jobId", jobId);
        response.put("message", "Simple job scheduled successfully");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/email")
    public ResponseEntity<Map<String, Object>> scheduleEmailJob(
            @RequestParam String recipient,
            @RequestParam String subject,
            @RequestParam String content) {

        var jobId = jobScheduler.enqueue(() -> manualJobService.sendEmail(recipient, subject, content));

        Map<String, Object> response = new HashMap<>();
        response.put("jobId", jobId);
        response.put("message", "Email job scheduled successfully");
        response.put("recipient", recipient);
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/process-data")
    public ResponseEntity<Map<String, Object>> scheduleDataProcessingJob(
            @RequestParam(defaultValue = "1") int processingType) {

        String dataId = "DATA_" + UUID.randomUUID().toString().substring(0, 8);
        var jobId = jobScheduler.enqueue(() -> manualJobService.processData(dataId, processingType));

        Map<String, Object> response = new HashMap<>();
        response.put("jobId", jobId);
        response.put("dataId", dataId);
        response.put("processingType", processingType);
        response.put("message", "Data processing job scheduled successfully");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/long-running")
    public ResponseEntity<Map<String, Object>> scheduleLongRunningJob(@RequestParam String taskName) {
        var jobId = jobScheduler.enqueue(() -> manualJobService.longRunningJob(taskName));

        Map<String, Object> response = new HashMap<>();
        response.put("jobId", jobId);
        response.put("taskName", taskName);
        response.put("message", "Long running job scheduled successfully");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/schedule-delayed")
    public ResponseEntity<Map<String, Object>> scheduleDelayedJob(
            @RequestParam String message,
            @RequestParam(defaultValue = "30") int delayInSeconds) {

        var jobId = jobScheduler.schedule(
                LocalDateTime.now().plusSeconds(delayInSeconds),
                () -> manualJobService.executeSimpleJob(message + " (Delayed)")
        );

        Map<String, Object> response = new HashMap<>();
        response.put("jobId", jobId);
        response.put("message", "Delayed job scheduled successfully");
        response.put("delayInSeconds", delayInSeconds);
        response.put("scheduledFor", LocalDateTime.now().plusSeconds(delayInSeconds));
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/schedule-recurring")
    public ResponseEntity<Map<String, Object>> scheduleRecurringJob(@RequestParam String message) {
        var jobId = jobScheduler.scheduleRecurrently(
                "recurring-simple-job",
                Cron.every5minutes(),
                ZoneId.systemDefault(),
                () -> manualJobService.executeSimpleJob(message + " (Recurring)")
        );

        Map<String, Object> response = new HashMap<>();
        response.put("jobId", jobId);
        response.put("message", "Recurring job scheduled successfully (every 5 minutes)");
        response.put("cron", "*/5 * * * *");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/recurring/{jobName}")
    public ResponseEntity<Map<String, Object>> deleteRecurringJob(@PathVariable String jobName) {
        jobScheduler.delete(JobId.parse(jobName));

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Recurring job deleted successfully");
        response.put("jobName", jobName);
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getJobStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "JobRunr is running");
        response.put("dashboard", "http://localhost:8000");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }
}