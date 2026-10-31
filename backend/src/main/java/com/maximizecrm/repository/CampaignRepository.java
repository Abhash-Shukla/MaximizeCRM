package com.maximizecrm.repository;

import com.maximizecrm.entity.Campaign;
import com.maximizecrm.entity.CampaignStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    List<Campaign> findAllByOrderByCreatedAtDesc();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("SELECT c FROM Campaign c " +
           "JOIN FETCH c.assignedUser u " +
           "WHERE (:search IS NULL OR :search = '' OR " +
           "       LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "       (c.description IS NOT NULL AND LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')))) AND " +
           "      (:status IS NULL OR c.status = :status) AND " +
           "      (:assignedUserId IS NULL OR u.id = :assignedUserId) " +
           "ORDER BY c.createdAt DESC")
    List<Campaign> searchCampaigns(
        @Param("search") String search,
        @Param("status") CampaignStatus status,
        @Param("assignedUserId") Long assignedUserId
    );
}

