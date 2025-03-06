package com.autoprov.autoprov.controllers;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.autoprov.autoprov.services.LogService;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


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
    
}
