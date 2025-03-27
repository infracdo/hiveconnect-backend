package com.autoprov.autoprov.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.autoprov.autoprov.security.jwt.JwtUtils;

@Service
public class LogService {
    private static final Logger logger = LoggerFactory.getLogger(LogService.class);
    private static final Logger apiAuditLogger = LoggerFactory.getLogger("apiAuditLogger"); // For audit logs (API_LOG)
    private static final Logger apiAccessLogger = LoggerFactory.getLogger("apiAccessLogger"); // For audit logs (API_LOG)
    private static final Logger apiErrorLogger = LoggerFactory.getLogger("apiErrorLogger"); // For error logs
                                                                                            // (ERROR_LOG)
    private static final Logger frontendLogger = LoggerFactory.getLogger("frontendLogger"); // For frontend logs

    @Autowired
    private JwtUtils jwtUtils;

    public void logInfo(String message) {
        logger.info(message);
    }

    public void logApiAudit(String user, String action, String method, String endpoint, String payload, String status,
            String ip, String client, String agent) {
        if (user == null || user.trim().equals("")) {
            user = "unknown";
        }
        if (action == null || action.trim().equals("")) {
            action = "accessed api endpoint"; //TODO; CHANGE TO 'ADDED XXX OR CHANGED AAA TO BBB'
        }
        if (payload == null || payload.trim().equals("")) {
            payload = "none";
        }

        if (client != null && client.contains("Bearer ")) {
            client = jwtUtils.getUserNameFromJwtToken(client.substring(7));
        } else {
            client = "none";
        }
        apiAuditLogger.info(String.format(
                "User: %s | Action: %s | Method: %s | Endpoint: %s | Payload: %s | Status: %s | IP: %s | Client: %s | Agent: %s",
                user, action, method, endpoint, payload, status, ip, client, agent));
    }

    public void logApiAccess(String user, String action, String method, String endpoint, String payload, String status,
            String ip, String client, String agent) {
        if (user == null || user.trim().equals("")) {
            user = "unknown";
        }
        if (action == null || action.trim().equals("")) {
            action = "accessed api endpoint";
        }
        if (payload == null || payload.trim().equals("")) {
            payload = "none";
        }

        if (client != null && client.contains("Bearer ")) {
            client = jwtUtils.getUserNameFromJwtToken(client.substring(7));
        } else {
            client = "none";
        }
        apiAccessLogger.info(String.format(
                "User: %s | Action: %s | Method: %s | Endpoint: %s | Payload: %s | Status: %s | IP: %s | Client: %s | Agent: %s",
                user, action, method, endpoint, payload, status, ip, client, agent));
    }

    public void logApiError(String user, String action, String method, String endpoint, String payload, String status,
            String message, StackTraceElement[] stacktrace, String ip, String client, String agent) {
        if (user == null || user.trim().equals("")) {
            user = "unknown";
        }
        if (action == null || action.trim().equals("")) {
            action = "accessed api endpoint";
        }
        if (payload == null || payload.trim().equals("")) {
            payload = "none";
        }
        if (client != null && client.contains("Bearer ")) {
            client = jwtUtils.getUserNameFromJwtToken(client.substring(7));
        } else {
            client = "none";
        }
        apiErrorLogger.error(String.format(
                "User: %s | Action: %s | Method: %s | Endpoint: %s | Payload: %s | Status: %s | Message: %s | StackTrace: %s | IP: %s | Client: %s | Agent: %s",
                user, action, method, endpoint, payload, status, message, stacktrace[0], ip, client,
                agent));
    }

    // Method for frontend logs
    public void logFrontendAction(String user, String action, String page, String details, String ip, String client,
            String agent) {
        if (user == null || user.trim().equals("")) {
            user = "unknown";
        }
        if (action == null || action.trim().equals("")) {
            action = "performed action";
        }
        if (details == null || details.trim().equals("")) {
            details = "none";
        }
        frontendLogger.info(String.format(
                "User: %s | Action: %s | Page: %s | Details: %s | IP: %s | Client: %s | Agent: %s",
                user, action, page, details, ip, client, agent));
        System.out.println("created frontend log");
    }

    public void logError(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    public void logDebug(String message) {
        logger.debug(message);
    }

    public void logWarn(String message) {
        logger.warn(message);
    }

    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Map<String, Object> createResponse(HttpStatus status, String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now().format(DATE_TIME_FORMATTER));
        response.put("status", status.value());
        response.put("message", message);
        return response;
    }

}
