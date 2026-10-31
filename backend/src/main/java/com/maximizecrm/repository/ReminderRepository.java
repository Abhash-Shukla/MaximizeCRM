package com.maximizecrm.repository;

import com.maximizecrm.entity.Reminder;
import com.maximizecrm.entity.ReminderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    boolean existsByLeadId(Long leadId);

    Optional<Reminder> findByLeadId(Long leadId);

    Optional<Reminder> findFirstByLeadIdOrderByCreatedAtDesc(Long leadId);

    List<Reminder> findByAssignedUserId(Long userId);

    void deleteByLeadId(Long leadId);

    @Query("""
        SELECT r FROM Reminder r
        LEFT JOIN FETCH r.lead l
        LEFT JOIN FETCH l.contact c
        LEFT JOIN FETCH r.assignedUser u
        WHERE (:search IS NULL OR :search = '' OR LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(r.notes) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:status IS NULL OR r.status = :status)
          AND (:assignedUserId IS NULL OR r.assignedUser.id = :assignedUserId)
          AND (:leadId IS NULL OR (r.lead IS NOT NULL AND r.lead.id = :leadId))
        ORDER BY CASE WHEN r.dueDate IS NULL THEN 1 ELSE 0 END, r.dueDate ASC, r.createdAt DESC
    """)
    List<Reminder> searchReminders(
        @Param("search") String search,
        @Param("status") ReminderStatus status,
        @Param("assignedUserId") Long assignedUserId,
        @Param("leadId") Long leadId
    );
}

