package com.autoprov.autoprov.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.autoprov.autoprov.dto.AbsTokenResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class AbsService {

    @Autowired
    private LogService logService;

    @Value("${absApiUrl}")
    private String absApiUrl;

    @Value("${absTokenUrl}")
    private String absTokenUrl;

    @Value("${absApiKey}")
    private String absApiKey;

    @Value("${absTokenUsername}")
    private String absTokenUsername;

    @Value("${absTokenPassword}")
    private String absTokenPassword;

    public String fetchAbsToken() {
        String apiUrl = absTokenUrl;

        // Create headers with Content-Type set to application/json
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("ABS-API-KEY", absApiKey);

        // Create a JSON request body
        StringBuilder jsonBody = new StringBuilder();

        jsonBody.append("{");
        jsonBody.append("\"username\":\"" + absTokenUsername + "\",");
        jsonBody.append("\"password\":\"" + absTokenPassword + "\"");
        jsonBody.append("}");

        String jsonRequestBody = jsonBody.toString();
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody, headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            AbsTokenResponse token = restTemplate
                    .exchange(apiUrl, HttpMethod.POST, requestEntity, AbsTokenResponse.class)
                    .getBody();

            return "Bearer " + token.getAccess_token();
        } catch (Exception e) {
            return "";
        }
    }

    public ResponseEntity<?> statusCallBack(String newStatus, String subscriberAccountNumber) {
        Map<String, String> response = new LinkedHashMap<>();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        try {
            String absStatus;

            if ("active".equalsIgnoreCase(newStatus)) {
                absStatus = "Activate";
            } else if ("onhold".equalsIgnoreCase(newStatus)) {
                absStatus = "on-hold";
            } else if ("deactivated".equalsIgnoreCase(newStatus)) {
                absStatus = "Deactivated";
            } else {
                response.put("timestamp", timestamp);
                response.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
                response.put("message", "Provided status is invalid");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            String absUrl = absApiUrl + subscriberAccountNumber;

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("ABS-API-KEY", absApiKey);
            headers.set("Authorization", fetchAbsToken());

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("status", absStatus);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            System.out.println("request entity " + entity);
            ResponseEntity<String> absResponse = restTemplate.exchange(absUrl,
                    HttpMethod.POST, entity,
                    String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(absResponse.getBody());
            String absMessage = jsonNode.path("message").asText();

            response.put("timestamp", timestamp);
            response.put("status", String.valueOf(absResponse.getStatusCode().value()));
            response.put("message", absMessage);
            return ResponseEntity.status(absResponse.getStatusCode()).body(response);
        } catch (Exception e) {
            response.put("timestamp", timestamp);
            response.put("status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            response.put("message", "An error occurred. " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
