package com.pmtrack.repository;

import com.pmtrack.model.Timesheet;
import com.pmtrack.model.TimesheetStatus;
import com.pmtrack.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimesheetRepository extends JpaRepository<Timesheet, Long> {
    List<Timesheet> findByUser(User user);
    List<Timesheet> findByUserId(Long userId);
    List<Timesheet> findByProjectId(Long projectId);
    List<Timesheet> findByTaskId(Long taskId);
    List<Timesheet> findByStatus(TimesheetStatus status);
    List<Timesheet> findByUserIdAndWorkDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    List<Timesheet> findByWorkDateBetween(LocalDate startDate, LocalDate endDate);
    java.util.Optional<Timesheet> findByUserIdAndProjectIdAndTaskIdAndWorkDate(Long userId, Long projectId, Long taskId, LocalDate workDate);

    @Query("SELECT t FROM Timesheet t WHERE t.project.projectManager.id = :managerId AND t.status IN ('SUBMITTED', 'RESUBMITTED', 'UNDER_REVIEW')")
    List<Timesheet> findPendingForManager(@Param("managerId") Long managerId);

    @Query("SELECT COALESCE(SUM(t.hoursWorked), 0.0) FROM Timesheet t WHERE t.project.id = :projectId AND t.status = 'APPROVED'")
    Double sumApprovedHoursByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT COALESCE(SUM(t.hoursWorked), 0.0) FROM Timesheet t WHERE t.task.id = :taskId AND t.status = 'APPROVED'")
    Double sumApprovedHoursByTaskId(@Param("taskId") Long taskId);

    @Query("SELECT COALESCE(SUM(t.hoursWorked), 0.0) FROM Timesheet t WHERE t.user.id = :userId AND t.status = 'APPROVED'")
    Double sumApprovedHoursByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(t.hoursWorked), 0.0) FROM Timesheet t WHERE t.user.id = :userId AND t.workDate BETWEEN :startDate AND :endDate")
    Double sumHoursByUserAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(t.hoursWorked), 0.0) FROM Timesheet t WHERE t.billable = :billable AND t.status = 'APPROVED'")
    Double sumApprovedHoursByBillable(@Param("billable") Boolean billable);
}
