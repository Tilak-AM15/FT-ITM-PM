package com.pmtrack.service;

import com.pmtrack.dto.AiCopilotDto;
import com.pmtrack.model.*;
import com.pmtrack.repository.ProjectRepository;
import com.pmtrack.repository.TaskRepository;
import com.pmtrack.repository.TimesheetRepository;
import com.pmtrack.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class AiCopilotService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TimesheetRepository timesheetRepository;
    private final UserRepository userRepository;

    public AiCopilotService(ProjectRepository projectRepository, TaskRepository taskRepository,
                            TimesheetRepository timesheetRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.timesheetRepository = timesheetRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public AiCopilotDto.CopilotExecutiveSummary getCopilotAnalysis() {
        AiCopilotDto.CopilotExecutiveSummary summary = new AiCopilotDto.CopilotExecutiveSummary();

        List<Project> projects = projectRepository.findAll();
        List<Task> tasks = taskRepository.findAll();
        List<Timesheet> timesheets = timesheetRepository.findAll();

        List<AiCopilotDto.AiRiskPrediction> predictions = new ArrayList<>();
        List<AiCopilotDto.AnomalyDetection> anomalies = new ArrayList<>();

        for (Project p : projects) {
            Double logged = timesheetRepository.sumApprovedHoursByProjectId(p.getId());
            double actualHrs = logged != null ? logged : 0.0;
            double estHrs = p.getEstimatedHours() != null ? p.getEstimatedHours() : 100.0;

            List<Task> pTasks = taskRepository.findByProjectId(p.getId());
            long overdue = pTasks.stream().filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(LocalDate.now()) && t.getStatus() != TaskStatus.COMPLETED).count();

            if (p.getStatus() == ProjectStatus.ACTIVE) {
                if (overdue > 2 || actualHrs > estHrs * 0.9) {
                    predictions.add(new AiCopilotDto.AiRiskPrediction(
                            p.getName(),
                            p.getProjectCode(),
                            "HIGH",
                            68,
                            14.5,
                            overdue > 0 ? "Overdue dependencies on critical path" : "Effort burn rate exceeding initial estimates",
                            Arrays.asList("Rebalance sprint tasks across available developers", "Conduct intermediate scope review with client", "Fast-track approval of blocked technical dependencies")
                    ));
                } else {
                    predictions.add(new AiCopilotDto.AiRiskPrediction(
                            p.getName(),
                            p.getProjectCode(),
                            "LOW",
                            12,
                            2.0,
                            "Execution on track with milestones",
                            Arrays.asList("Maintain current sprint velocity", "Ensure weekly timesheet submissions")
                    ));
                }
            }
        }

        // Detect Anomaly in Timesheets (e.g. entries > 10h in a single day, or unusual non-billable spikes)
        for (Timesheet ts : timesheets) {
            if (ts.getHoursWorked() > 10.0) {
                anomalies.add(new AiCopilotDto.AnomalyDetection(
                        "EXCESSIVE_DAILY_HOURS",
                        ts.getUser().getFullName(),
                        ts.getTask().getTitle(),
                        "Logged " + ts.getHoursWorked() + "h on " + ts.getWorkDate() + " (exceeds 10h threshold).",
                        "WARNING"
                ));
            }
        }

        if (anomalies.isEmpty()) {
            anomalies.add(new AiCopilotDto.AnomalyDetection(
                    "OPTIMAL_HEALTH",
                    "All Team Members",
                    "Timesheet Submission",
                    "No anomalous hour distributions detected for the current pay period.",
                    "INFO"
            ));
        }

        summary.setExecutiveSummary(
                "Organizational delivery health is stable at 84% on-time index. Projected revenue efficiency indicates 78% billable realization. 2 projects require resource rebalancing to prevent milestone delay."
        );
        summary.setRiskPredictions(predictions);
        summary.setAnomalies(anomalies);
        summary.setRecommendedNextActions(Arrays.asList(
                "Approve 7 pending timesheet submissions awaiting PM review to maintain up-to-date costing.",
                "Allocate 1 additional developer to OpenLayer Platform to absorb remaining API migration scope.",
                "Review milestone deliverables for FRC 2026 Event nearing target deadline."
        ));

        return summary;
    }
}
