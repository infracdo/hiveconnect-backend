package com.autoprov.autoprov.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/test")
public class TestController {
  @GetMapping("/all")
  public String allAccess() {
    return "Public Content.";
  }

  @GetMapping("/user")
  @PreAuthorize("hasAnyRole('ROLE_HIVECONNECT_ADMIN', 'ROLE_HIVECONNECT_NETWORK_SUPPORT', 'ROLE_HIVECONNECT_TECH_SUPPORT')")
  public String userAccess() {
    return "User Content. ....congrats you can access this content.";
  }

  @GetMapping("/mod")
  @PreAuthorize("hasRole('ROLE_HIVECONNECT_NETWORK_SUPPORT')")
  public String moderatorAccess() {
    return "Network Support Board.";
  }

  @GetMapping("/admin")
  @PreAuthorize("hasRole('ROLE_HIVECONNECT_ADMIN')")
  public String adminAccess() {
    return "Hiveconnect Admin Board.";
  }




}