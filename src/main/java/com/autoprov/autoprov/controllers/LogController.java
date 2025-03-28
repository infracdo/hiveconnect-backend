package com.autoprov.autoprov.controllers;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> logFrontendAction(@RequestBody Map<String, Object> logData, HttpServletRequest request) {
        try {
            String user = (String) logData.get("user");
            String action = (String) logData.get("action");
            String details = logData.get("details").toString();
            String page = (String) logData.get("page");
            String userAgent = (String) logData.get("userAgent");

            // Get IP address from the request
            String ip = request.getRemoteAddr();

            // Log the frontend action using the LogService
            logService.logFrontendAction(user, action, page, details, ip, "web", userAgent);

            return ResponseEntity.ok("Frontend log saved successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    logService.createResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                            "An error occurred. " + e.getMessage()));
        }
    }

    // @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    // public ResponseEntity<Map<String, String>>
    // handleOptionsRequest(HttpServletResponse response) {
    // response.setStatus(HttpServletResponse.SC_OK);
    // response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"); // Set your
    // response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT,
    // DELETE, OPTIONS"); // Allowed HTTP
    // response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Content-Type,
    // Authorization"); // Allowed request
    // response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"); //
    // Allow credentials (cookies, auth
    // Map<String, String> responseBody = new LinkedHashMap<>();
    // responseBody.put("message", "CORS preflight request successful");
    // return ResponseEntity.ok(responseBody);
    // }

}
