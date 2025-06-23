package com.dishant.tasks.management.scheduler;

import com.dishant.tasks.management.model.SchedulerLog;
import com.dishant.tasks.management.model.Task;
import com.dishant.tasks.management.repository.SchedulerLogRepository;
import com.dishant.tasks.management.repository.TaskRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
@Slf4j
@AllArgsConstructor
public class TaskSchedulerService {

    private TaskLockService taskLockService;
    private TaskRepository taskRepository;
    private SchedulerLogRepository schedulerLogRepository;

    @Scheduled(cron = "${scheduler.cron}")
    public void runScheduler() {
        Timestamp startTime = new Timestamp(System.currentTimeMillis());
        log.info("Scheduler running now...");

        List<Task> dueTasks = taskRepository.findByDueDateLessThanEqual(LocalDateTime.now());
        log.info("Total tasks found for closure with due date: {}", dueTasks.size());

        String systemName = getSystemHostName();

        for (Task task : dueTasks) {
            log.info("Attempting to put lock on and close the task with id: {}", task.getId());
            boolean success = taskLockService.lockAndExecuteTask(task);
            String status;
            String remarks;

            if (success) {
                status = "SUCCESS";
                remarks = "Task closed as due date expired.";
                log.info("Successfully closed task with id: {}", task.getId());
            } else {
                status = "FAILURE";
                remarks = "Task closing failure as Task is currently locked or handled by another process.";
                log.info("Skipped closing task with id: {}. Task is currently locked or another process is handling it.", task.getId());
            }
            Timestamp endTime = new Timestamp(System.currentTimeMillis());
            logSchedulerJobExecution(systemName, task.getId(), status, startTime, endTime, remarks);
        }
    }

    public void logSchedulerJobExecution(String systemName, Long taskId, String status, Timestamp startTime, Timestamp endTime, String remarks) {
        log.info("Scheduler Execution - System Name: {}, Task ID: {}, Status: {}, Start Time: {}, End Time: {}, Remarks: {}", systemName, taskId, status, startTime, endTime, remarks);
        SchedulerLog schedulerLog = SchedulerLog.builder()
                .taskId(taskId)
                .systemName(systemName)
                .status(status)
                .startTime(startTime)
                .endTime(endTime)
                .remarks(remarks)
                .build();

        schedulerLogRepository.save(schedulerLog);
    }

    private String getSystemHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            log.warn("Unable to retrieve hostname, defaulting to 'unknown-host'", e);
            return "unknown-host";
        }
    }
}
