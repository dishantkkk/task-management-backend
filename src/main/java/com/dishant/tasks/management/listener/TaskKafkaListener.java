package com.dishant.tasks.management.listener;

import com.dishant.tasks.management.model.Task;
import com.dishant.tasks.management.model.TaskStatus;
import com.dishant.tasks.management.model.User;
import com.dishant.tasks.management.repository.TaskRepository;
import com.dishant.tasks.management.repository.UserRepository;
import com.example.kafka.avro.TaskEventSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.dishant.tasks.management.constants.Constants.UNFLAGGED;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskKafkaListener {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @KafkaListener(topics = "task-topic", groupId = "task-group")
    public void consume(ConsumerRecord<String, TaskEventSchema> eventRecord) {
        TaskEventSchema event = eventRecord.value();
        log.info("Processing task event from Kafka for user '{}': {}", event.getUserName(), event.getTitle());

        LocalDateTime dueDate;
        try {
            dueDate = LocalDateTime.parse(event.getDueDate());
        } catch (Exception e) {
            log.error("Invalid due date format '{}', skipping task: {}", event.getDueDate(), event.getTitle(), e);
            return;
        }

        Long userId = event.getUserId();
        User user = userRepository.findById(userId)
                .orElse(null);

        if (user == null) {
            log.error("User not found for ID '{}'. Task '{}' skipped.", userId, event.getTitle());
            return;
        }

        if (!user.getUsername().equals(event.getUserName())) {
            log.error("Username mismatch for user ID '{}'. Expected '{}', but got '{}'.", userId, user.getUsername(), event.getUserName());
            return;
        }

        Task task = Task.builder()
                .title(event.getTitle())
                .description(event.getDescription())
                .dueDate(dueDate)
                .flag(UNFLAGGED)
                .status(TaskStatus.PENDING)
                .user(user)
                .build();

        Task saved = taskRepository.save(task);
        log.info("Task '{}' saved successfully from Kafka for user '{}'", saved.getTitle(), user.getUsername());
    }
}
