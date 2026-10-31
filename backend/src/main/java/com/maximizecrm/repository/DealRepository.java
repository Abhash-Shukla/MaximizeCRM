package com.maximizecrm.repository;

import com.maximizecrm.entity.Deal;
import com.maximizecrm.entity.DealStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DealRepository extends JpaRepository<Deal, Long> {

    boolean existsByLeadId(Long leadId);

    boolean existsByLeadIdAndIdNot(Long leadId, Long id);

    Optional<Deal> findByLeadId(Long leadId);

    List<Deal> findAllByOrderByCreatedAtDesc();

    @Query("SELECT d FROM Deal d " +
           "JOIN FETCH d.lead l " +
           "JOIN FETCH l.contact c " +
           "JOIN FETCH d.assignedUser u " +
           "WHERE (:search IS NULL OR :search = '' OR " +
           "       LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "       LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "       (c.company IS NOT NULL AND LOWER(c.company) LIKE LOWER(CONCAT('%', :search, '%'))) OR " +
           "       (d.notes IS NOT NULL AND LOWER(d.notes) LIKE LOWER(CONCAT('%', :search, '%')))) AND " +
           "      (:stage IS NULL OR d.stage = :stage) AND " +
           "      (:assignedUserId IS NULL OR u.id = :assignedUserId) " +
           "ORDER BY d.createdAt DESC")
    List<Deal> searchDeals(
        @Param("search") String search,
        @Param("stage") DealStage stage,
        @Param("assignedUserId") Long assignedUserId
    );

    @Query("SELECT d.lead.id FROM Deal d")
    List<Long> findAllLeadIdsWithDeals();
}

