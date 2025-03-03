package com.autoprov.autoprov.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LogService {
    private static final Logger logger = LoggerFactory.getLogger(LogService.class);
    private static final Logger apiLogger = LoggerFactory.getLogger("apiLogger"); // For audit logs (API_LOG)
    private static final Logger apiErrorLogger = LoggerFactory.getLogger("apiErrorLogger"); // For error logs (ERROR_LOG)

    public void logInfo(String message) {
        logger.info(message);
    }

    public void logApiAccess(String user, String action, String method, String endpoint, String payload, String status, String ip, String client, String agent) {
        if (user == null || user.trim().equals("")) {
            user = "unknown";
        }
        if (action == null || action.trim().equals("")) {
            action = "accessed api endpoint";
        }
        if (payload == null || payload.trim().equals("")) {
            payload = "none";
        }
        apiLogger.info(String.format(
            "User: %s | Action: %s | Method: %s | Endpoint: %s | Payload: %s | Status: %s | IP: %s | Client: %s | Agent: %s",
            user, action, method, endpoint, payload, status, ip, client, agent
        ));
        System.out.println("created access log");
    }

    public void logApiError(String user, String action, String method, String endpoint, String payload, String status, String message, StackTraceElement[] stacktrace, String ip, String client, String agent) {
        if (user == null || user.trim().equals("")) {
            user = "unknown";
        }
        if (action == null || action.trim().equals("")) {
            action = "accessed api endpoint";
        }
        if (payload == null || payload.trim().equals("")) {
            payload = "none";
        }
        apiErrorLogger.error(String.format(
            "User: %s | Action: %s | Method: %s | Endpoint: %s | Payload: %s | Status: %s | Message: %s | StackTrace: %s | IP: %s | Client: %s | Agent: %s",
            user, action, method, endpoint, payload, status, message, stacktrace[stacktrace.length - 1], ip, client, agent
        ));
        System.out.println("created error log");
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
}
