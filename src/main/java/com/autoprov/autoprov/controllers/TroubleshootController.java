package com.autoprov.autoprov.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.autoprov.autoprov.security.jwt.JwtUtils;
import com.autoprov.autoprov.services.LogService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin(origins = "*")
@RestController
public class TroubleshootController {

    @Value("${prometheusApiUrl}")
    private static String prometheusApiUrl;

    @Autowired
    private LogService logService;

    @Async("asyncExecutor")
    @GetMapping("/getStatus/{device}")
    public String getOnuStatus(@PathVariable("device") String device, @RequestParam(required = false) String user,
            @RequestParam(required = false) String action, HttpServletRequest request) {

        String device_name = "{job=\"ip_address\",site_tenant=\"DCTECH\",device_name=\""
                + device + "\"}";
        String prometheusUrl = prometheusApiUrl + device_name;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity requestEntity = new HttpEntity("", headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(prometheusUrl, HttpMethod.GET, requestEntity,
                String.class, device_name);

        String responseBody = response.getBody();
        System.out.println(responseBody);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            JsonNode valueNode = jsonNode
                    .path("data")
                    .path("result")
                    .get(0)
                    .path("value")
                    .get(1);

            String value = valueNode.asText();
            System.out.println("Value: " + value);

            // if (value.equals("1")) {
            // return "Online";
            // } else {
            // return "Offline";
            // }

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), device,
                    String.valueOf(response.getStatusCode().value()), request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return value;
        } catch (Exception e) {
            e.printStackTrace();
            logService.logApiError(user, action, request.getMethod(), request.getRequestURI(), device,
                    String.valueOf(response.getStatusCode().value()), e.getMessage(), e.getStackTrace(),
                    request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

        }

        return responseBody;
    }
}
