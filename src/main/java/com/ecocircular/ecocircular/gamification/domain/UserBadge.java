package com.ecocircular.ecocircular.gamification.domain;

import com.ecocircular.ecocircular.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "user_badges",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_user_badge_tenant",
        columnNames = {"user_id", "badge_id", "tenant_id"}
    )
)
@Getter @Setter @NoArgsConstructor
public class UserBadge extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    @Column(name = "earned_at", nullable = false)
    private LocalDateTime earnedAt;

    public UserBadge(UUID userId, Badge badge, LocalDateTime earnedAt) {
        this.userId   = userId;
        this.badge    = badge;
        this.earnedAt = earnedAt;
    }
}
