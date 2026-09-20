package com.devsync.aiservice.ai.repository;

import com.devsync.aiservice.ai.entity.AIRequestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AIRequestLogRepository extends JpaRepository<AIRequestLog, UUID> {
}
