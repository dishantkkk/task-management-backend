package com.dishant.tasks.management.scheduler;

import com.dishant.tasks.management.model.Task;
import com.dishant.tasks.management.service.TaskService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@AllArgsConstructor
public class TaskLockService {

    private final LockProvider lockProvider;
    private final TaskService taskService;
    private RedisTemplate<String, String> redisTemplate;

    public boolean lockAndExecuteTask(Task task) {
        String lockName = "closeTaskWithDueDateCompletion_"+ task.getId();
        LockConfiguration lockConfig = new LockConfiguration(
                Instant.now(),
                lockName,
                Duration.ofMinutes(2), // at most
                Duration.ofMinutes(1) // at least
        );

        log.info("Attempting to acquire lock for task: {}", task.getId());

        Optional<SimpleLock> lock = lockProvider.lock(lockConfig);
        ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        Set<String> keys = redisTemplate.keys("*");
        for (String key : keys) {
            String value = valueOps.get(key);
            log.info("Redis Key: {}, with Value: {}", key, value);
        }
        if (lock.isPresent()) {
            try {
                log.info("Lock acquired successfully for task id: {}", task.getId());
                taskService.closeTask(task.getId());
                log.info("Task Closure completed for task id: {}", task.getId());
                return true;
            } finally {
                lock.get().unlock();
                log.info("Lock released for task id: {}", task.getId());
            }
        } else {
            log.warn("Failed to acquire lock for task id: {}. Task may be processed by another instance.", task.getId());
            return false;
        }
    }
}
