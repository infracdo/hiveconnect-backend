package com.autoprov.autoprov.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.tomcat.util.json.JSONParser;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.util.JSONPObject;

@CrossOrigin(origins = "*")
@RestController
public class AcsController {

    // Exposed for HiveApp ----------------------------------------
    @Async("AsyncExecutor")
    @PostMapping("/getRogueDevices")
    public static JSONParser getRougeDevices(@RequestBody Map<String, String> params) {
        // TODO: Call to ACS to Disconnect Wan2
        String apiUrl = "http://172.91.0.136:7547/getRogueDevices";

        StringBuilder jsonBody = new StringBuilder();
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(apiUrl, String.class);
        JSONParser parser = new JSONParser(response);
        return parser;

    }

    // Exposed for HiveApp (end) ----------------------------------------

    // -------------------Exposed APIs for Connect-Disconnect
    @Async("AsyncExecutor")
    @PostMapping("/temporaryDisconnectClient")
    public static ResponseEntity<Map<String, String>> disconnectClient(@RequestBody Map<String, String> params) {
        // TODO: Call to ACS to Disconnect Wan2
        String apiUrl = "http://172.91.0.136:7547/toggleWan";

        // Create headers with Content-Type set to application/json
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String instance = "2";
        String toggle = "0";

        // Create a JSON request body
        StringBuilder jsonBody = new StringBuilder();

        jsonBody.append("{");
        jsonBody.append("\"serialNumber\":\"" + params.get("serialNumber") + "\",");
        jsonBody.append("\"Instance\":\"" + instance + "\",");
        jsonBody.append("\"Toggle\":\"" + toggle + "\"");
        jsonBody.append("}");

        String jsonRequestBody = jsonBody.toString();
        System.out.println(jsonRequestBody);
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        String jsonResponse = restTemplate.postForObject(apiUrl, requestEntity, String.class);

        System.out.println("HiveConnect: ACS Push: WAN2 Disable Task Pushed");
        System.out.println("Response: " + jsonResponse);

        // WAN2 Toggling Pushed on Success
        // Fault Error on Error

        if (jsonResponse.contains("Pushed")) {

            Map<String, String> response = new HashMap<>();
            response.put("status", "200");
            response.put("message", jsonResponse);
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } else

        {
            Map<String, String> response = new HashMap<>();
            response.put("status", "500");
            response.put("message", jsonResponse);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Async("AsyncExecutor")
    @PostMapping("/reconnectClient")
    public static ResponseEntity<Map<String, String>> reconnectClient(@RequestBody Map<String, String> params) {
        // TODO: Call to ACS to Disconnect Wan2
        String apiUrl = "http://172.91.0.136:7547/toggleWan";

        // Create headers with Content-Type set to application/json
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String instance = "2";
        String toggle = "1";

        // Create a JSON request body
        StringBuilder jsonBody = new StringBuilder();

        jsonBody.append("{");
        jsonBody.append("\"serialNumber\":\"" + params.get("serialNumber") + "\",");
        jsonBody.append("\"Instance\":\"" + instance + "\",");
        jsonBody.append("\"Toggle\":\"" + toggle + "\"");

        jsonBody.append("}");

        String jsonRequestBody = jsonBody.toString();
        System.out.println(jsonRequestBody);
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        String jsonResponse = restTemplate.postForObject(apiUrl, requestEntity, String.class);

        System.out.println("HiveConnect: ACS Push: WAN2 Enable Task Pushed");
        System.out.println("Response: " + jsonResponse);

        if (jsonResponse.contains("Pushed")) {

            Map<String, String> response = new HashMap<>();
            response.put("status", "200");
            response.put("message", jsonResponse);
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } else

        {
            Map<String, String> response = new HashMap<>();
            response.put("status", "500");
            response.put("message", jsonResponse);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // permanently disconnect
    @Async("AsyncExecutor")
    @PostMapping("/permanentDisconnect")
    public static ResponseEntity<Map<String, String>> permanentDisconnectClient(
            @RequestBody Map<String, String> params) {
        // TODO: Call to ACS to REMOVE WAN2
        String apiUrl = "http://172.91.0.136:7547/deleteWanInstance";

        // Create headers with Content-Type set to application/json
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String instance = "2";

        // Create a JSON request body
        StringBuilder jsonBody = new StringBuilder();

        jsonBody.append("{");
        jsonBody.append("\"serialNumber\":\"" + params.get("serialNumber") + "\",");
        jsonBody.append("\"Instance\":\"" + instance + "\"");

        jsonBody.append("}");

        String jsonRequestBody = jsonBody.toString();
        System.out.println(jsonRequestBody);
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        String jsonResponse = restTemplate.postForObject(apiUrl, requestEntity, String.class);

        System.out.println("HiveConnect: ACS Push: WAN2 Enable Task Pushed");
        System.out.println("Response: " + jsonResponse);

        if (jsonResponse.contains("Successful")) {

            Map<String, String> response = new HashMap<>();
            response.put("status", "200");
            response.put("message", jsonResponse);
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } else

        {
            Map<String, String> response = new HashMap<>();
            response.put("status", "500");
            response.put("message", jsonResponse);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ----------- Controller Functions ---- On ERRORS

    // Rollback WAN2, delete WAN2. Applicable after succeeding errors

    static String deleteWanInstance(String serialNumber) {
        String apiUrl = "http://172.91.0.136:7547/deleteWanInstance";

        // Create headers with Content-Type set to application/json
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create a JSON request body
        StringBuilder jsonBody = new StringBuilder();

        jsonBody.append("{");
        jsonBody.append("\"serialNumber\":\"" + serialNumber + "\",");
        jsonBody.append("\"instance\":\"" + "2" + "\"");
        jsonBody.append("}");

        String jsonRequestBody = jsonBody.toString();
        System.out.println(jsonRequestBody);
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        String jsonResponse = restTemplate.postForObject(apiUrl, requestEntity, String.class);

        System.out.println("HiveConnect: ACS Task Rollback for " + serialNumber);
        System.out.println("Response: " + jsonResponse);

        return "ACS Task Rollback";
    }

    // Rollback SSID, return to default. Applicable after succeeding erors
    public static String rollbackSsid(String serialNumber) {
        String apiUrl = "http://172.91.0.136:7547/rollbackSsid";

        // Create headers with Content-Type set to application/json
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create a JSON request body
        StringBuilder jsonBody = new StringBuilder();

        jsonBody.append("{");
        jsonBody.append("\"serialNumber\":\"" + serialNumber + "\"");
        jsonBody.append("}");

        String jsonRequestBody = jsonBody.toString();
        System.out.println(jsonRequestBody);
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        String jsonResponse = restTemplate.postForObject(apiUrl, requestEntity, String.class);

        System.out.println("HiveConnect: ACS Server Removed " + serialNumber + " from Rogue");
        System.out.println("Response: " + jsonResponse);

        return "HiveConnect: ACS Server SSID Rollback pushed for " + serialNumber;
    }

    // ----------- Controller Functions ---- On Success
    // Unrogue ONU on ACS
    public static String onuOnboarded(String serialNumber) {
        String apiUrl = "http://172.91.0.136:7547/onuOnboarded";

        // Create headers with Content-Type set to application/json
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create a JSON request body
        StringBuilder jsonBody = new StringBuilder();

        jsonBody.append("{");
        jsonBody.append("\"serialNumber\":\"" + serialNumber + "\"");
        jsonBody.append("}");

        String jsonRequestBody = jsonBody.toString();
        System.out.println(jsonRequestBody);
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        String jsonResponse = restTemplate.postForObject(apiUrl, requestEntity, String.class);

        System.out.println("HiveConnect: ACS Server Removed " + serialNumber + " from Rogue");
        System.out.println("Response: " + jsonResponse);

        return "HiveConnect: ACS Server Removed " + serialNumber + " from Rogue";
    }

    // Set inform interval for 600 seconds Post Successful Provisioning
    public static String setInformIntervalPostProv(String serialNumber) {
        String apiUrl = "http://172.91.0.136:7547/setInformInterval";

        // Create headers with Content-Type set to application/json
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create a JSON request body
        StringBuilder jsonBody = new StringBuilder();

        jsonBody.append("{");
        jsonBody.append("\"serialNumber\":\"" + serialNumber + "\",");
        jsonBody.append("\"time\":\"" + "600" + "\"");
        jsonBody.append("}");

        String jsonRequestBody = jsonBody.toString();
        System.out.println(jsonRequestBody);
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        String jsonResponse = restTemplate.postForObject(apiUrl, requestEntity, String.class);

        System.out.println("HiveConnect: Set Inform Interval");
        System.out.println("Response: " + jsonResponse);

        return "Provisioning Complete";
    }

}
