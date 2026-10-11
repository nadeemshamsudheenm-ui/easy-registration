package com.easyreg.config;

import com.easyreg.entity.RegistrationApplication;
import com.easyreg.entity.User;
import com.easyreg.enums.RegistrationStatus;
import com.easyreg.enums.Role;
import com.easyreg.repository.RegistrationApplicationRepository;
import com.easyreg.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Initializes demo users and seed applications on startup if database is empty.
 * Provides pre-configured accounts for all roles.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RegistrationApplicationRepository applicationRepository;

    public DataInitializer(UserRepository userRepository,
                           RegistrationApplicationRepository applicationRepository) {
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            seedUsersAndApplications();
        }
    }

    private void seedUsersAndApplications() {
        // 1. Department Staff Users
        User accountsStaff = new User("accounts_admin", "password123", "Mr. Rajesh Kumar",
                "accounts@college.edu", "+91 98765 43210", Role.ACCOUNTS);
        accountsStaff.setDepartment("Finance & Accounts");
        userRepository.save(accountsStaff);

        User transportStaff = new User("transport_admin", "password123", "Mr. Suresh Nair",
                "transport@college.edu", "+91 98765 43211", Role.TRANSPORT);
        transportStaff.setDepartment("Campus Transport Services");
        userRepository.save(transportStaff);

        User labStaff = new User("lab_admin", "password123", "Mrs. Priya Sharma",
                "lab@college.edu", "+91 98765 43212", Role.LAB_ASSISTANT);
        labStaff.setDepartment("Computer Science Labs");
        userRepository.save(labStaff);

        User hodCs = new User("hod_cs", "password123", "Dr. K. V. Ramanathan",
                "hod.cse@college.edu", "+91 98765 43213", Role.HOD);
        hodCs.setDepartment("Computer Science & Engineering");
        userRepository.save(hodCs);

        // 2. Demo Students
        User student1 = new User("2024CS101", "password123", "Aravind Krishna",
                "aravind.cs@student.college.edu", "+91 98450 11223", Role.STUDENT);
        student1.setDepartment("Computer Science & Engineering");
        student1.setBatchYear("2024-2028");
        student1.setCurrentSemester(4);
        student1.setRollNumber("CS-401");
        student1.setTransportOpted(true);
        student1.setBusRoute("Route 12 - Central City to College");
        userRepository.save(student1);

        User student2 = new User("2024CS102", "password123", "Meera Nandakumar",
                "meera.cs@student.college.edu", "+91 98450 11224", Role.STUDENT);
        student2.setDepartment("Computer Science & Engineering");
        student2.setBatchYear("2024-2028");
        student2.setCurrentSemester(4);
        student2.setRollNumber("CS-402");
        student2.setTransportOpted(false);
        userRepository.save(student2);

        User student3 = new User("2024CS103", "password123", "Rahul Menon",
                "rahul.cs@student.college.edu", "+91 98450 11225", Role.STUDENT);
        student3.setDepartment("Computer Science & Engineering");
        student3.setBatchYear("2024-2028");
        student3.setCurrentSemester(4);
        student3.setRollNumber("CS-403");
        student3.setTransportOpted(true);
        student3.setBusRoute("Route 05 - Green Hills to College");
        userRepository.save(student3);

        // 3. Seed an active application for Student 1 (Aravind Krishna) waiting at Accounts
        RegistrationApplication app1 = new RegistrationApplication();
        app1.setApplicationNumber("REG-2026-S4-1001");
        app1.setStudent(student1);
        app1.setAcademicYear("2025-2026");
        app1.setTargetSemester(4);
        app1.setDepartment("Computer Science & Engineering");
        app1.setCgpa(8.72);
        app1.setBacklogsCount(0);
        app1.setTransportOpted(true);
        app1.setBusRoute("Route 12 - Central City to College");
        app1.setBusStop("St. Xavier Junction");
        app1.setFeeReceiptNumber("RCPT-2026-88412");
        app1.setFeeAmountPaid(45000.00);
        app1.setFeePaymentDate(LocalDate.now().minusDays(2));
        app1.setTransportFeeReceipt("TR-2026-9931");
        app1.setTransportFeeAmount(12000.00);
        app1.setSelectedSubjects("CS401: Database Management Systems, CS402: Operating Systems, CS403: Design & Analysis of Algorithms, CS404: Web Technologies, CS405: Software Engineering, CS431: DBMS Lab, CS432: OS Lab");
        app1.setStatus(RegistrationStatus.PENDING_ACCOUNTS);
        app1.setSubmittedAt(LocalDateTime.now().minusHours(5));
        applicationRepository.save(app1);

        // 4. Seed an already fully APPROVED application for Student 3 to immediately demonstrate the printable registration card!
        RegistrationApplication appApproved = new RegistrationApplication();
        appApproved.setApplicationNumber("REG-2026-S4-1003");
        appApproved.setStudent(student3);
        appApproved.setAcademicYear("2025-2026");
        appApproved.setTargetSemester(4);
        appApproved.setDepartment("Computer Science & Engineering");
        appApproved.setCgpa(9.10);
        appApproved.setBacklogsCount(0);
        appApproved.setTransportOpted(true);
        appApproved.setBusRoute("Route 05 - Green Hills to College");
        appApproved.setBusStop("Rose Garden Stop");
        appApproved.setFeeReceiptNumber("RCPT-2026-77312");
        appApproved.setFeeAmountPaid(45000.00);
        appApproved.setFeePaymentDate(LocalDate.now().minusDays(4));
        appApproved.setTransportFeeReceipt("TR-2026-8812");
        appApproved.setTransportFeeAmount(12000.00);
        appApproved.setSelectedSubjects("CS401: Database Management Systems, CS402: Operating Systems, CS403: Design & Analysis of Algorithms, CS404: Web Technologies, CS405: Software Engineering, CS431: DBMS Lab, CS432: OS Lab");
        
        // Mark all clearances
        appApproved.setAccountsCleared(true);
        appApproved.setAccountsClearedAt(LocalDateTime.now().minusDays(3));
        appApproved.setAccountsApprovedBy("Mr. Rajesh Kumar");
        appApproved.setAccountsRemarks("Tuition fee payment verified via UTR #90214812");

        appApproved.setTransportCleared(true);
        appApproved.setTransportClearedAt(LocalDateTime.now().minusDays(2));
        appApproved.setTransportApprovedBy("Mr. Suresh Nair");
        appApproved.setTransportRemarks("Annual bus pass pass-code #BP-4401 assigned");

        appApproved.setLabCleared(true);
        appApproved.setLabClearedAt(LocalDateTime.now().minusDays(1));
        appApproved.setLabApprovedBy("Mrs. Priya Sharma");
        appApproved.setLabRemarks("All hardware kit dues and lab logs cleared");

        appApproved.setHodApproved(true);
        appApproved.setHodApprovedAt(LocalDateTime.now().minusHours(2));
        appApproved.setHodApprovedBy("Dr. K. V. Ramanathan");
        appApproved.setHodRemarks("Academics verified. Approved for Semester 4.");

        appApproved.setStatus(RegistrationStatus.APPROVED);
        appApproved.setSubmittedAt(LocalDateTime.now().minusDays(4));
        appApproved.setCompletedAt(LocalDateTime.now().minusHours(2));
        applicationRepository.save(appApproved);
    }
}
