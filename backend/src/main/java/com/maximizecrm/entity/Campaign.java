package com.maximizecrm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "campaigns")
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CampaignType type = CampaignType.EMAIL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CampaignStatus status = CampaignStatus.PLANNING;

    @Column
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @Column(precision = 12, scale = 2)
    private BigDecimal budget = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal expectedRevenue = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal actualCost = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assigned_user_id", nullable = false)
    private User assignedUser;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    public Campaign() {
    }

    public Campaign(
        String name,
        CampaignType type,
        CampaignStatus status,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal budget,
        BigDecimal expectedRevenue,
        BigDecimal actualCost,
        User assignedUser,
        String description
    ) {
        this.name = name;
        this.type = type != null ? type : CampaignType.EMAIL;
        this.status = status != null ? status : CampaignStatus.PLANNING;
        this.startDate = startDate;
        this.endDate = endDate;
        this.budget = budget != null ? budget : BigDecimal.ZERO;
        this.expectedRevenue = expectedRevenue != null ? expectedRevenue : BigDecimal.ZERO;
        this.actualCost = actualCost != null ? actualCost : BigDecimal.ZERO;
        this.assignedUser = assignedUser;
        this.description = description;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = CampaignStatus.PLANNING;
        }
        if (this.type == null) {
            this.type = CampaignType.EMAIL;
        }
        if (this.budget == null) {
            this.budget = BigDecimal.ZERO;
        }
        if (this.expectedRevenue == null) {
            this.expectedRevenue = BigDecimal.ZERO;
        }
        if (this.actualCost == null) {
            this.actualCost = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CampaignType getType() {
        return type;
    }

    public void setType(CampaignType type) {
        this.type = type;
    }

    public CampaignStatus getStatus() {
        return status;
    }

    public void setStatus(CampaignStatus status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public BigDecimal getExpectedRevenue() {
        return expectedRevenue;
    }

    public void setExpectedRevenue(BigDecimal expectedRevenue) {
        this.expectedRevenue = expectedRevenue;
    }

    public BigDecimal getActualCost() {
        return actualCost;
    }

    public void setActualCost(BigDecimal actualCost) {
        this.actualCost = actualCost;
    }

    public User getAssignedUser() {
        return assignedUser;
    }

    public void setAssignedUser(User assignedUser) {
        this.assignedUser = assignedUser;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

