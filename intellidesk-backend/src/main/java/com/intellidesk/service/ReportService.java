package com.intellidesk.service;

import com.intellidesk.dto.ChartDataDto;
import com.intellidesk.dto.DashboardSummaryDto;
import com.intellidesk.dto.ReportSummaryDto;

public interface ReportService {

    DashboardSummaryDto getDashboardSummary();

    ReportSummaryDto getReportSummary();

    ChartDataDto getTicketsByCategory();

    ChartDataDto getTicketsByPriority();

    ChartDataDto getTicketsByStatus();

    ChartDataDto getTicketsByMonth();
}
