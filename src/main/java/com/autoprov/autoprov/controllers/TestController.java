package com.autoprov.autoprov.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autoprov.autoprov.entity.hiveDomain.VlanInfo;
import com.autoprov.autoprov.repositories.hiveRepositories.VlanInfoRepository;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/test")
public class TestController {

  @Autowired
  private VlanInfoRepository vlanInfoRepo;

  // @GetMapping("/all")
  // public String allAccess() {
  //   return "Public Content.";
  // }

  // @GetMapping("/user")
  // // @PreAuthorize("hasAnyRole('HIVECONNECT_ADMIN', 'HIVECONNECT_NETWORK_SUPPORT',
  // // 'HIVECONNECT_TECH_SUPPORT')")
  // public String userAccess() {
  //   VlanInfo newInfo = VlanInfo.builder().accountNo("reseller1")
  //               .vlanId("2012")
  //               .location("CDO")
  //               .build();
                
  //               vlanInfoRepo.save(newInfo);
  //   return "User Content. ....congrats you can access this content.";
  // }

  // @GetMapping("/mod")
  // // @PreAuthorize("hasRole('HIVECONNECT_NETWORK_SUPPORT')")
  // @PreAuthorize("hasRole('MODERATOR')")
  // public String moderatorAccess() {
  //   return "Network Support Board.";
  // }

  // @GetMapping("/admin")
  // // @PreAuthorize("hasAuthority('ROLE_HIVECONNECT_ADMIN')")
  // @PreAuthorize("hasRole('ADMIN')")
  // public String adminAccess() {
  //   return "Hiveconnect Admin Board.";
  // }
}