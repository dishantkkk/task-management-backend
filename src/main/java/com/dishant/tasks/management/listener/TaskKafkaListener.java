package com.dishant.tasks.management.listener;

import com.dishant.tasks.management.dto.TaskRequest;
import com.dishant.tasks.management.model.Task;
import com.dishant.tasks.management.model.TaskStatus;
import com.dishant.tasks.management.model.User;
import com.dishant.tasks.management.repository.TaskRepository;
import com.dishant.tasks.management.repository.UserRepository;
import com.dishant.tasks.management.service.TaskService;
import com.example.kafka.avro.TaskEventSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskKafkaListener {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @KafkaListener(topics = "task-topic", groupId = "task-group")
    public void consume(ConsumerRecord<String, TaskEventSchema> eventRecord) {
        TaskEventSchema event = eventRecord.value();
        log.info("📥 Received task event for user '{}': {}", event.getUserName(), event.getTitle());
        LocalDate dueDate = null;
        try {
            dueDate = LocalDate.parse(event.getDueDate());
        } catch (Exception e) {
            log.error("❌Invalid due date format: {}", event.getDueDate(), e);
            return;
        }
        Long userId = event.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found for ID: " + userId));
        if(!user.getUsername().equals(event.getUserName())) {
            log.error("Wrong user!");
            return;
        }

        Task task = Task.builder()
                .title(event.getTitle())
                .description(event.getDescription())
                .dueDate(dueDate)
                .status(TaskStatus.PENDING)
                .user(user)
                .build();

        Task saved = taskRepository.save(task);
        log.info("✅ Task created from Kafka: {}", saved.getTitle());
    }
}
