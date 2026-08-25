package com.pmtrack.repository;

import com.pmtrack.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findAllByOrderByTimestampDesc();
    List<AuditLog> findByEntityNameOrderByTimestampDesc(String entityName);
    List<AuditLog> findByEntityNameAndEntityIdOrderByTimestampDesc(String entityName, Long entityId);
    List<AuditLog> findByUsernameOrderByTimestampDesc(String username);
    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);
}
