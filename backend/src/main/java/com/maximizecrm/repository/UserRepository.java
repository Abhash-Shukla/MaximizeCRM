package com.maximizecrm.repository;

import com.maximizecrm.entity.Role;
import com.maximizecrm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    List<User> findAllByOrderByCreatedAtDesc();

    @Query("""
        SELECT u FROM User u
        WHERE (:search IS NULL OR :search = '' OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:role IS NULL OR u.role = :role)
          AND (:active IS NULL OR u.active = :active)
        ORDER BY u.createdAt DESC
    """)
    List<User> searchUsers(
        @Param("search") String search,
        @Param("role") Role role,
        @Param("active") Boolean active
    );
}

