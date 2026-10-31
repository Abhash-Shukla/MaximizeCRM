package com.maximizecrm.repository;

import com.maximizecrm.entity.Lead;
import com.maximizecrm.entity.LeadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {

    boolean existsByContactId(Long contactId);

    List<Lead> findAllByOrderByCreatedAtDesc();

    @Query("SELECT l FROM Lead l " +
           "JOIN FETCH l.contact c " +
           "JOIN FETCH l.assignedUser u " +
           "WHERE (:search IS NULL OR :search = '' OR " +
           "       LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "       (c.company IS NOT NULL AND LOWER(c.company) LIKE LOWER(CONCAT('%', :search, '%'))) OR " +
           "       LOWER(l.source) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "       (l.notes IS NOT NULL AND LOWER(l.notes) LIKE LOWER(CONCAT('%', :search, '%')))) AND " +
           "      (:status IS NULL OR l.status = :status) AND " +
           "      (:assignedUserId IS NULL OR u.id = :assignedUserId) " +
           "ORDER BY l.createdAt DESC")
    List<Lead> searchLeads(
        @Param("search") String search,
        @Param("status") LeadStatus status,
        @Param("assignedUserId") Long assignedUserId
    );
}

