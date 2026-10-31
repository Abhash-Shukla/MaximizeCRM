package com.maximizecrm.repository;

import com.maximizecrm.entity.Contact;
import com.maximizecrm.entity.ContactCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, Long id);

    List<Contact> findAllByOrderByCreatedAtDesc();

    @Query("SELECT c FROM Contact c WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           " LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " (c.email IS NOT NULL AND LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%'))) OR " +
           " (c.phone IS NOT NULL AND LOWER(c.phone) LIKE LOWER(CONCAT('%', :search, '%'))) OR " +
           " (c.company IS NOT NULL AND LOWER(c.company) LIKE LOWER(CONCAT('%', :search, '%')))) AND " +
           "(:category IS NULL OR c.category = :category) " +
           "ORDER BY c.createdAt DESC")
    List<Contact> searchContacts(@Param("search") String search, @Param("category") ContactCategory category);
}

