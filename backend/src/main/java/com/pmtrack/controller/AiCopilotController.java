package com.pmtrack.controller;

import com.pmtrack.dto.AiCopilotDto;
import com.pmtrack.service.AiCopilotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/ai-copilot")
@Tag(name = "AI Copilot", description = "AI Project Assistant, Delay & Risk Prediction, and Anomaly Detection")
public class AiCopilotController {

    private final AiCopilotService aiCopilotService;

    public AiCopilotController(AiCopilotService aiCopilotService) {
        this.aiCopilotService = aiCopilotService;
    }

    @GetMapping("/executive-summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','PROJECT_MANAGER','MANAGEMENT')")
    @Operation(summary = "Get AI-generated risk predictions, timesheet anomalies, and recommendations")
    public ResponseEntity<AiCopilotDto.CopilotExecutiveSummary> getExecutiveSummary() {
        return ResponseEntity.ok(aiCopilotService.getCopilotAnalysis());
    }
}
