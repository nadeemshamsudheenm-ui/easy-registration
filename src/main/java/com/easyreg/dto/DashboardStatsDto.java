package com.easyreg.dto;

public class DashboardStatsDto {
    private long totalApplications;
    private long pendingAccounts;
    private long pendingTransport;
    private long pendingLab;
    private long pendingHod;
    private long totalApproved;
    private long totalRejected;

    public DashboardStatsDto() {}

    public long getTotalApplications() {
        return totalApplications;
    }

    public void setTotalApplications(long totalApplications) {
        this.totalApplications = totalApplications;
    }

    public long getPendingAccounts() {
        return pendingAccounts;
    }

    public void setPendingAccounts(long pendingAccounts) {
        this.pendingAccounts = pendingAccounts;
    }

    public long getPendingTransport() {
        return pendingTransport;
    }

    public void setPendingTransport(long pendingTransport) {
        this.pendingTransport = pendingTransport;
    }

    public long getPendingLab() {
        return pendingLab;
    }

    public void setPendingLab(long pendingLab) {
        this.pendingLab = pendingLab;
    }

    public long getPendingHod() {
        return pendingHod;
    }

    public void setPendingHod(long pendingHod) {
        this.pendingHod = pendingHod;
    }

    public long getTotalApproved() {
        return totalApproved;
    }

    public void setTotalApproved(long totalApproved) {
        this.totalApproved = totalApproved;
    }

    public long getTotalRejected() {
        return totalRejected;
    }

    public void setTotalRejected(long totalRejected) {
        this.totalRejected = totalRejected;
    }
}
