package com.dishant.tasks.management.repository;

import com.dishant.tasks.management.model.Task;
import com.dishant.tasks.management.model.TaskStatus;
import com.dishant.tasks.management.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByDueDateLessThanEqual(LocalDateTime dateTime);
    List<Task> findByUserAndStatusNot(User user, TaskStatus status);
    List<Task> findByStatusNot(TaskStatus status);
    long countByStatus(TaskStatus status);
}
