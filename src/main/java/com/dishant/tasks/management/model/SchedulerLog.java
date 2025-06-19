package com.dishant.tasks.management.model;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "scheduler_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchedulerLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long taskId;

    private String systemName;

    private String status;

    private Timestamp startTime;

    private Timestamp endTime;

    @Column(length = 500)
    private String remarks;
}
