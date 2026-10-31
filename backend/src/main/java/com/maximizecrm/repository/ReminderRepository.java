package com.maximizecrm.repository;

import com.maximizecrm.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
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
}

