package com.easyreg.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebPageController {

    @GetMapping("/")
    public String indexPage() {
        return "forward:/index.html";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "forward:/login.html";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "forward:/register.html";
    }

    @GetMapping("/student-dashboard")
    public String studentDashboard() {
        return "forward:/student-dashboard.html";
    }

    @GetMapping("/staff-dashboard")
    public String staffDashboard() {
        return "forward:/staff-dashboard.html";
    }

    @GetMapping("/print-slip")
    public String printSlipPage() {
        return "forward:/print-slip.html";
    }
}
