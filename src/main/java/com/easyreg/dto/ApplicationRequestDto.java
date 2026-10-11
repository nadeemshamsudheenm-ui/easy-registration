package com.easyreg.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class ApplicationRequestDto {

    @NotBlank(message = "Academic Year is required (e.g. 2025-2026)")
    private String academicYear;

    @NotNull(message = "Target Semester is required")
    private Integer targetSemester;

    @NotBlank(message = "Department is required")
    private String department;

    @NotNull(message = "CGPA is required")
    @DecimalMin(value = "0.0", message = "CGPA cannot be negative")
    @DecimalMax(value = "10.0", message = "CGPA cannot exceed 10.0")
    private Double cgpa;

    private Integer backlogsCount = 0;

    private Boolean transportOpted = false;

    private String busRoute;

    private String busStop;

    @NotBlank(message = "Fee Receipt Number is required")
    private String feeReceiptNumber;

    @NotNull(message = "Fee Amount Paid is required")
    private Double feeAmountPaid;

    private LocalDate feePaymentDate;

    // Optional transport fee details if student opts for bus
    private String transportFeeReceipt;
    private Double transportFeeAmount;

    @NotBlank(message = "Please enter or select your registered courses")
    private String selectedSubjects;

    public ApplicationRequestDto() {}

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public Integer getTargetSemester() {
        return targetSemester;
    }

    public void setTargetSemester(Integer targetSemester) {
        this.targetSemester = targetSemester;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Double getCgpa() {
        return cgpa;
    }

    public void setCgpa(Double cgpa) {
        this.cgpa = cgpa;
    }

    public Integer getBacklogsCount() {
        return backlogsCount;
    }

    public void setBacklogsCount(Integer backlogsCount) {
        this.backlogsCount = backlogsCount;
    }

    public Boolean getTransportOpted() {
        return transportOpted;
    }

    public void setTransportOpted(Boolean transportOpted) {
        this.transportOpted = transportOpted;
    }

    public String getBusRoute() {
        return busRoute;
    }

    public void setBusRoute(String busRoute) {
        this.busRoute = busRoute;
    }

    public String getBusStop() {
        return busStop;
    }

    public void setBusStop(String busStop) {
        this.busStop = busStop;
    }

    public String getFeeReceiptNumber() {
        return feeReceiptNumber;
    }

    public void setFeeReceiptNumber(String feeReceiptNumber) {
        this.feeReceiptNumber = feeReceiptNumber;
    }

    public Double getFeeAmountPaid() {
        return feeAmountPaid;
    }

    public void setFeeAmountPaid(Double feeAmountPaid) {
        this.feeAmountPaid = feeAmountPaid;
    }

    public LocalDate getFeePaymentDate() {
        return feePaymentDate;
    }

    public void setFeePaymentDate(LocalDate feePaymentDate) {
        this.feePaymentDate = feePaymentDate;
    }

    public String getTransportFeeReceipt() {
        return transportFeeReceipt;
    }

    public void setTransportFeeReceipt(String transportFeeReceipt) {
        this.transportFeeReceipt = transportFeeReceipt;
    }

    public Double getTransportFeeAmount() {
        return transportFeeAmount;
    }

    public void setTransportFeeAmount(Double transportFeeAmount) {
        this.transportFeeAmount = transportFeeAmount;
    }

    public String getSelectedSubjects() {
        return selectedSubjects;
    }

    public void setSelectedSubjects(String selectedSubjects) {
        this.selectedSubjects = selectedSubjects;
    }
}
