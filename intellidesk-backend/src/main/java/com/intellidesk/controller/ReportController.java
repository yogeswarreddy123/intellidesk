package com.intellidesk.controller;

import com.intellidesk.dto.ApiResponse;
import com.intellidesk.dto.ChartDataDto;
import com.intellidesk.dto.DashboardSummaryDto;
import com.intellidesk.dto.ReportSummaryDto;
import com.intellidesk.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping({"/api/dashboard/summary", "/api/reports/dashboard"})
    public ResponseEntity<ApiResponse<DashboardSummaryDto>> getDashboardSummary() {
        DashboardSummaryDto summary = reportService.getDashboardSummary();
        return ResponseEntity.ok(ApiResponse.success("Dashboard metrics retrieved", summary));
    }

    @GetMapping("/api/reports/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReportSummaryDto>> getReportSummary() {
        ReportSummaryDto summary = reportService.getReportSummary();
        return ResponseEntity.ok(ApiResponse.success("Report analytics retrieved", summary));
    }

    @GetMapping("/api/reports/by-category")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ChartDataDto>> getTicketsByCategory() {
        ChartDataDto chartData = reportService.getTicketsByCategory();
        return ResponseEntity.ok(ApiResponse.success("Category report data retrieved", chartData));
    }

    @GetMapping("/api/reports/by-priority")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ChartDataDto>> getTicketsByPriority() {
        ChartDataDto chartData = reportService.getTicketsByPriority();
        return ResponseEntity.ok(ApiResponse.success("Priority report data retrieved", chartData));
    }

    @GetMapping("/api/reports/by-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ChartDataDto>> getTicketsByStatus() {
        ChartDataDto chartData = reportService.getTicketsByStatus();
        return ResponseEntity.ok(ApiResponse.success("Status report data retrieved", chartData));
    }

    @GetMapping("/api/reports/by-month")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ChartDataDto>> getTicketsByMonth() {
        ChartDataDto chartData = reportService.getTicketsByMonth();
        return ResponseEntity.ok(ApiResponse.success("Monthly report data retrieved", chartData));
    }
}
