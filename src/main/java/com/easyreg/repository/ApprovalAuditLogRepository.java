package com.easyreg.repository;

import com.easyreg.entity.ApprovalAuditLog;
import com.easyreg.entity.RegistrationApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalAuditLogRepository extends JpaRepository<ApprovalAuditLog, Long> {
    List<ApprovalAuditLog> findByApplicationOrderByActionTimestampDesc(RegistrationApplication application);
}
