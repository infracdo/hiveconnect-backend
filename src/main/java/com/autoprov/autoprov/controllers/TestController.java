package com.autoprov.autoprov.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.autoprov.autoprov.payload.request.SignupRequest;
import com.autoprov.autoprov.payload.response.MessageResponse;
import com.autoprov.autoprov.security.jwt.JwtUtils;
import com.autoprov.autoprov.services.LogService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/test")
public class TestController {

  @Autowired
  JwtUtils jwtUtils;

  @Autowired
  private LogService logService;

  @GetMapping("/all")
  public String allAccess() {
    return "Public Content.";
  }

  @PostMapping("/some")
  public ResponseEntity<?> someAccess() {

        // String method = request.getMethod();
        // String uri = request.getRequestURI();
        // String ip = request.getRemoteAddr();
        // String token = request.getHeader("Authorization").substring(7);
        // String agent = request.getHeader("User-Agent");

    // logService.logApiAccess(user, action, method, uri, signupRequest.getUsername(),
    //     String.valueOf(HttpStatus.OK.value()), ip,
    //     jwtUtils.getUserNameFromJwtToken(token),
    //     agent);

    return ResponseEntity
        .ok()
        .body(null);
  }

  // @GetMapping("/user")
  // // @PreAuthorize("hasAnyRole('HIVECONNECT_ADMIN',
  // 'HIVECONNECT_NETWORK_SUPPORT',
  // // 'HIVECONNECT_TECH_SUPPORT')")
  // public String userAccess() {
  // return "User Content. ....congrats you can access this content.";
  // }

  // @GetMapping("/mod")
  // // @PreAuthorize("hasRole('HIVECONNECT_NETWORK_SUPPORT')")
  // @PreAuthorize("hasRole('MODERATOR')")
  // public String moderatorAccess() {
  // return "Network Support Board.";
  // }

  // @GetMapping("/admin")
  // // @PreAuthorize("hasAuthority('ROLE_HIVECONNECT_ADMIN')")
  // @PreAuthorize("hasRole('ADMIN')")
  // public String adminAccess() {
  // return "Hiveconnect Admin Board.";
  // }
}