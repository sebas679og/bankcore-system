package com.bankcore.customers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Main entry point for the bankcore-customers application. */
@SpringBootApplication
@SuppressWarnings("PMD.UseUtilityClass")
public class MsCustomersApplication {

  public static void main(String[] args) {
    SpringApplication.run(MsCustomersApplication.class, args);
  }
}
