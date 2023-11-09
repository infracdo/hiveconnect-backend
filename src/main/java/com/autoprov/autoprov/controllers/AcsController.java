package com.autoprov.autoprov.controllers;

import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

public class AcsController {
    public static String deleteWanInstance(String serialNumber) {
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

    public static String setInformInterval(String serialNumber) {
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

    // Connect-Disconnect
    @Async("AsyncExecutor")
    @PostMapping("/temporaryDisconnectClient")
    public static String disconnectClient(@RequestBody Map<String, String> params) {
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

        return jsonResponse;
    }

    @Async("AsyncExecutor")
    @PostMapping("/reconnectClient")
    public static String reconnectClient(@RequestBody Map<String, String> params) {
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

        return jsonResponse;
    }

    // permanently disconnect
    @Async("AsyncExecutor")
    @PostMapping("/permanentDisconnect")
    public static String permanentDisconnectClient(@RequestBody Map<String, String> params) {
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

        return jsonResponse;
    }

}
