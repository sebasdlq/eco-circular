package com.ecocircular.ecocircular.gamification.domain;

import com.ecocircular.ecocircular.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "user_missions",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_user_mission_tenant",
        columnNames = {"user_id", "mission_id", "tenant_id"}
    )
)
@Getter @Setter @NoArgsConstructor
public class UserMission extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MissionStatus status;

    @Column(name = "current_value", nullable = false)
    private double currentValue;

    @Column(name = "progress_percent", nullable = false)
    private int progressPercent;

    @Column(name = "deadline")
    private LocalDate deadline;

    /** Fecha en que se completó la misión; null si aún está en progreso */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public UserMission(UUID userId, Mission mission,
                       MissionStatus status, double currentValue,
                       int progressPercent, LocalDate deadline) {
        this.userId          = userId;
        this.mission         = mission;
        this.status          = status;
        this.currentValue    = currentValue;
        this.progressPercent = progressPercent;
        this.deadline        = deadline;
        this.completedAt     = status == MissionStatus.COMPLETADA
                                ? LocalDateTime.now() : null;
    }
}
