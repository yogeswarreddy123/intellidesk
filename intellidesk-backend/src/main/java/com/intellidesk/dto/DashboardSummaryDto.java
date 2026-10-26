package com.intellidesk.dto;

import java.util.List;
import java.util.Map;

public class DashboardSummaryDto {

    // Common / Employee
    private long totalMyTickets;
    private long myOpenTickets;
    private long myInProgressTickets;
    private long myResolvedTickets;

    // Agent / Admin
    private long assignedTickets;
    private long totalTickets;
    private long openTickets;
    private long inProgressTickets;
    private long resolvedTickets;
    private long closedTickets;
    private long criticalTickets;
    private long activeAgents;
    private long totalUsers;

    // Recent items
    private List<TicketDto> recentTickets;
    private List<AuditLogDto> recentActivities;

    // Category / Status distribution for agent/admin
    private Map<String, Long> ticketsByCategory;
    private Map<String, Long> ticketsByPriority;
    private Map<String, Long> ticketsByStatus;

    public DashboardSummaryDto() {
    }

    public long getTotalMyTickets() {
        return totalMyTickets;
    }

    public void setTotalMyTickets(long totalMyTickets) {
        this.totalMyTickets = totalMyTickets;
    }

    public long getMyOpenTickets() {
        return myOpenTickets;
    }

    public void setMyOpenTickets(long myOpenTickets) {
        this.myOpenTickets = myOpenTickets;
    }

    public long getMyInProgressTickets() {
        return myInProgressTickets;
    }

    public void setMyInProgressTickets(long myInProgressTickets) {
        this.myInProgressTickets = myInProgressTickets;
    }

    public long getMyResolvedTickets() {
        return myResolvedTickets;
    }

    public void setMyResolvedTickets(long myResolvedTickets) {
        this.myResolvedTickets = myResolvedTickets;
    }

    public long getAssignedTickets() {
        return assignedTickets;
    }

    public void setAssignedTickets(long assignedTickets) {
        this.assignedTickets = assignedTickets;
    }

    public long getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(long totalTickets) {
        this.totalTickets = totalTickets;
    }

    public long getOpenTickets() {
        return openTickets;
    }

    public void setOpenTickets(long openTickets) {
        this.openTickets = openTickets;
    }

    public long getInProgressTickets() {
        return inProgressTickets;
    }

    public void setInProgressTickets(long inProgressTickets) {
        this.inProgressTickets = inProgressTickets;
    }

    public long getResolvedTickets() {
        return resolvedTickets;
    }

    public void setResolvedTickets(long resolvedTickets) {
        this.resolvedTickets = resolvedTickets;
    }

    public long getClosedTickets() {
        return closedTickets;
    }

    public void setClosedTickets(long closedTickets) {
        this.closedTickets = closedTickets;
    }

    public long getCriticalTickets() {
        return criticalTickets;
    }

    public void setCriticalTickets(long criticalTickets) {
        this.criticalTickets = criticalTickets;
    }

    public long getActiveAgents() {
        return activeAgents;
    }

    public void setActiveAgents(long activeAgents) {
        this.activeAgents = activeAgents;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public List<TicketDto> getRecentTickets() {
        return recentTickets;
    }

    public void setRecentTickets(List<TicketDto> recentTickets) {
        this.recentTickets = recentTickets;
    }

    public List<AuditLogDto> getRecentActivities() {
        return recentActivities;
    }

    public void setRecentActivities(List<AuditLogDto> recentActivities) {
        this.recentActivities = recentActivities;
    }

    public Map<String, Long> getTicketsByCategory() {
        return ticketsByCategory;
    }

    public void setTicketsByCategory(Map<String, Long> ticketsByCategory) {
        this.ticketsByCategory = ticketsByCategory;
    }

    public Map<String, Long> getTicketsByPriority() {
        return ticketsByPriority;
    }

    public void setTicketsByPriority(Map<String, Long> ticketsByPriority) {
        this.ticketsByPriority = ticketsByPriority;
    }

    public Map<String, Long> getTicketsByStatus() {
        return ticketsByStatus;
    }

    public void setTicketsByStatus(Map<String, Long> ticketsByStatus) {
        this.ticketsByStatus = ticketsByStatus;
    }
}
