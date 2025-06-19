package com.dishant.tasks.management.repository;

import com.dishant.tasks.management.model.SchedulerLog;
import com.dishant.tasks.management.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchedulerLogRepository extends JpaRepository<SchedulerLog, Long> {
}
