package com.autoprov.autoprov.controllers;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.autoprov.autoprov.services.LogService;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@CrossOrigin(origins = "*")
@RestController
public class LogController {

    @Autowired
    private LogService logService;

    @PostMapping("/log-frontend-action")
    public String logFrontendAction(@RequestBody Map<String, Object> logData, HttpServletRequest request) {
        String user = (String) logData.get("user");
        String action = (String) logData.get("action");
        String details = logData.get("details").toString();
        String page = (String) logData.get("page");
        String userAgent = (String) logData.get("userAgent");

        // Get IP address from the request
        String ip = request.getRemoteAddr();

        // Log the frontend action using the LogService
        logService.logFrontendAction(user, action, page, details, ip, "web", userAgent);

        return "Frontend log saved successfully.";
    }

    // @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    // public ResponseEntity<Map<String, String>> handleOptionsRequest(HttpServletResponse response) {
    //     response.setStatus(HttpServletResponse.SC_OK);
    //     response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"); // Set your
    //     response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS"); // Allowed HTTP
    //     response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Content-Type, Authorization"); // Allowed request
    //     response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"); // Allow credentials (cookies, auth
    //     Map<String, String> responseBody = new LinkedHashMap<>();
    //     responseBody.put("message", "CORS preflight request successful");
    //     return ResponseEntity.ok(responseBody);
    // }
    
}
