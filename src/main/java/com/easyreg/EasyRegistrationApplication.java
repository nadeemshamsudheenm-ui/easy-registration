package com.easyreg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EasyRegistrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(EasyRegistrationApplication.class, args);
        System.out.println("==================================================================");
        System.out.println("  EASY REGISTRATION SYSTEM IS RUNNING ON http://localhost:8080   ");
        System.out.println("==================================================================");
    }
}
