package com.pmtrack.dto;

import java.util.List;

public class AiCopilotDto {

    public static class AiRiskPrediction {
        private String projectName;
        private String projectCode;
        private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL
        private int delayProbabilityPercentage;
        private double predictedCostOverrunPercentage;
        private String primaryRiskFactor;
        private List<String> mitigationRecommendations;

        public AiRiskPrediction() {}
        public AiRiskPrediction(String projectName, String projectCode, String riskLevel, int delayProbabilityPercentage, double predictedCostOverrunPercentage, String primaryRiskFactor, List<String> mitigationRecommendations) {
            this.projectName = projectName;
            this.projectCode = projectCode;
            this.riskLevel = riskLevel;
            this.delayProbabilityPercentage = delayProbabilityPercentage;
            this.predictedCostOverrunPercentage = predictedCostOverrunPercentage;
            this.primaryRiskFactor = primaryRiskFactor;
            this.mitigationRecommendations = mitigationRecommendations;
        }

        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }
        public String getProjectCode() { return projectCode; }
        public void setProjectCode(String projectCode) { this.projectCode = projectCode; }
        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
        public int getDelayProbabilityPercentage() { return delayProbabilityPercentage; }
        public void setDelayProbabilityPercentage(int delayProbabilityPercentage) { this.delayProbabilityPercentage = delayProbabilityPercentage; }
        public double getPredictedCostOverrunPercentage() { return predictedCostOverrunPercentage; }
        public void setPredictedCostOverrunPercentage(double predictedCostOverrunPercentage) { this.predictedCostOverrunPercentage = predictedCostOverrunPercentage; }
        public String getPrimaryRiskFactor() { return primaryRiskFactor; }
        public void setPrimaryRiskFactor(String primaryRiskFactor) { this.primaryRiskFactor = primaryRiskFactor; }
        public List<String> getMitigationRecommendations() { return mitigationRecommendations; }
        public void setMitigationRecommendations(List<String> mitigationRecommendations) { this.mitigationRecommendations = mitigationRecommendations; }
    }

    public static class AnomalyDetection {
        private String anomalyType; // "EXCESSIVE_HOURS", "UNUSUAL_BILLABLE_RATIO", "OVERLAPPING_LOGS", "MISSING_TIMESHEET"
        private String employeeName;
        private String projectOrTaskName;
        private String description;
        private String severity; // INFO, WARNING, ALERT

        public AnomalyDetection() {}
        public AnomalyDetection(String anomalyType, String employeeName, String projectOrTaskName, String description, String severity) {
            this.anomalyType = anomalyType;
            this.employeeName = employeeName;
            this.projectOrTaskName = projectOrTaskName;
            this.description = description;
            this.severity = severity;
        }

        public String getAnomalyType() { return anomalyType; }
        public void setAnomalyType(String anomalyType) { this.anomalyType = anomalyType; }
        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
        public String getProjectOrTaskName() { return projectOrTaskName; }
        public void setProjectOrTaskName(String projectOrTaskName) { this.projectOrTaskName = projectOrTaskName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
    }

    public static class CopilotExecutiveSummary {
        private String executiveSummary;
        private List<AiRiskPrediction> riskPredictions;
        private List<AnomalyDetection> anomalies;
        private List<String> recommendedNextActions;

        public CopilotExecutiveSummary() {}

        public String getExecutiveSummary() { return executiveSummary; }
        public void setExecutiveSummary(String executiveSummary) { this.executiveSummary = executiveSummary; }
        public List<AiRiskPrediction> getRiskPredictions() { return riskPredictions; }
        public void setRiskPredictions(List<AiRiskPrediction> riskPredictions) { this.riskPredictions = riskPredictions; }
        public List<AnomalyDetection> getAnomalies() { return anomalies; }
        public void setAnomalies(List<AnomalyDetection> anomalies) { this.anomalies = anomalies; }
        public List<String> getRecommendedNextActions() { return recommendedNextActions; }
        public void setRecommendedNextActions(List<String> recommendedNextActions) { this.recommendedNextActions = recommendedNextActions; }
    }
}
