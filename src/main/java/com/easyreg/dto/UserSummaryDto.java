package com.easyreg.dto;

import com.easyreg.entity.User;
import com.easyreg.enums.Role;

public class UserSummaryDto {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private String department;
    private String batchYear;
    private Integer currentSemester;
    private String rollNumber;
    private Boolean transportOpted;
    private String busRoute;

    public UserSummaryDto() {}

    public static UserSummaryDto fromEntity(User user) {
        UserSummaryDto dto = new UserSummaryDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setDepartment(user.getDepartment());
        dto.setBatchYear(user.getBatchYear());
        dto.setCurrentSemester(user.getCurrentSemester());
        dto.setRollNumber(user.getRollNumber());
        dto.setTransportOpted(user.getTransportOpted());
        dto.setBusRoute(user.getBusRoute());
        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getBatchYear() {
        return batchYear;
    }

    public void setBatchYear(String batchYear) {
        this.batchYear = batchYear;
    }

    public Integer getCurrentSemester() {
        return currentSemester;
    }

    public void setCurrentSemester(Integer currentSemester) {
        this.currentSemester = currentSemester;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
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
}
