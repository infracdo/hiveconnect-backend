package com.autoprov.autoprov.controllers;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.autoprov.autoprov.domain.Client;
import com.autoprov.autoprov.domain.IpAddress;
import com.autoprov.autoprov.repositories.ClientRepository;
import com.autoprov.autoprov.repositories.IpAddressRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.*;

@CrossOrigin(origins = "*")
@RestController
public class AutoProvisionController {
    // Insert playbook invokes here
    @Autowired
    private IpAddressRepository ipAddRepo;

    @Autowired
    private ClientRepository clientRepo;

    @Async("AsyncExecutor")
    @PostMapping("/executeProvision")
    public String executeProvision(@RequestBody Map<String, String> params) {

        System.out.println("HiveService: Provision executed");

        // Prepare RequestBody Values
        String accountNo = params.get("accountNo");
        String clientName = params.get("clientName");

        String serialNumber = params.get("serialNumber");
        String macAddress = params.get("macAddress");
        String ipAddress = params.get("ipAddress");
        String oltIp = params.get("olt");
        String defaultGateway = ipAddRepo.getGatewayOfIpAddress(ipAddress.substring(0, (ipAddress.lastIndexOf("."))));
        String packageType = params.get("packageType");

        // ACS Processes
        Optional<IpAddress> ipAddressData = ipAddRepo.findByipAddress(ipAddress);
        Integer vlanId = ipAddressData.get().getVlanId();
        pushToACS(clientName, serialNumber, defaultGateway, ipAddress, vlanId);

        // Ansible Process
        executeAnsible(accountNo, serialNumber, macAddress, clientName, ipAddress, packageType, oltIp);

        return "Provision Pushed";

    }

    @Async("AsyncExecutor")
    @PostMapping("/temporaryDisconnectClient")
    public String disconnectClient(@RequestBody Map<String, String> params) {
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
        jsonBody.append("\"SN\":\"" + params.get("serialNumber") + "\",");
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

        return null;
    }

    @Async("AsyncExecutor")
    @PostMapping("/reconnectClient")
    public String reconnectClient(@RequestBody Map<String, String> params) {
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
        jsonBody.append("\"SN\":\"" + params.get("serialNumber") + "\",");
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

        return null;
    }

    public String pushToACS(String clientName, String serialNumber, String defaultGateway, String ipAddress,
            Integer vlanId) {
        // Define the API URL
        String apiUrl = "http://172.91.0.136:7547/executeAutoConfig";

        // Create headers with Content-Type set to application/json
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create a JSON request body
        StringBuilder jsonBody = new StringBuilder();

        jsonBody.append("{");
        jsonBody.append("\"clientName\":\"" + clientName + "\",");
        jsonBody.append("\"serialNumber\":\"" + serialNumber + "\",");
        jsonBody.append("\"defaultGateway\":\"" + defaultGateway + "\",");
        jsonBody.append("\"ipAddress\":\"" + ipAddress + "\",");
        jsonBody.append("\"vlanId\":\"" + vlanId + "\"");
        jsonBody.append("}");

        String jsonRequestBody = jsonBody.toString();
        System.out.println(jsonRequestBody);
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        String jsonResponse = restTemplate.postForObject(apiUrl, requestEntity, String.class);

        System.out.println("HiveConnect: ACS Push executed");
        System.out.println("Response: " + jsonResponse);

        return null;
    }

    public String executeAnsible(String accountNo, String serialNumber, String macAddress, String clientName,
            String onu_private_ip, String packageType, String oltIp) {

        String ansibleApiUrl = "http://172.91.10.189/api/v2/job_templates/9/launch/";
        String accessToken = "6NHpotS8gptsgnbZM2B4yiFQHQq7mz";

        String deviceName = "" + clientName.replace(" ", "_") + "_bw1";
        System.out.println(deviceName);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = "{\n" +
                "\"job_template\": \"9\",\n" +
                "\"ask_variables_on_launch\": \"true\",\n" +
                "\"extra_vars\": \"---" +
                "\\nserial_number: " + serialNumber +
                "\\ndevice_name: " + deviceName +
                "\\nmac_address: " + macAddress +
                "\\nolt_ip: " + oltIp +
                "\\naccount_number: null " +
                "\\nstatus: Activated " +
                "\\nonu_private_ip: " + onu_private_ip +
                "\\ndownstream: 11000" +
                "\\npackage_type: " + packageType +
                "\\nupstream: 11000\""
                +
                "}";
        System.out.println(requestBody);

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(ansibleApiUrl, HttpMethod.POST, requestEntity,
                String.class);

        System.out.println("HiveConnect: Ansible executed");

        if (response.getStatusCode() == HttpStatus.CREATED) {
            System.out.println("Request successful. Response: " + response.getBody());

            // update ipAddress table
            ipAddRepo.associateIpAddressToAccountNumber(accountNo, onu_private_ip);

            Optional<Client> optionalClient = clientRepo.findClientBySerialNumber(serialNumber);
            if (optionalClient.isPresent()) {
                Client client = optionalClient.get();
                client.setOnuDeviceName(deviceName);
                clientRepo.save(client);
            }

        } else {
            System.out.println("Request failed. Response: " + response.getStatusCode());
        }
        return requestBody;
    }

    @Async("AsyncExecutor")
    @GetMapping("/lastStatusJob")
    public String lastStatusJob(String jobId) throws JsonMappingException, JsonProcessingException {

        String ansibleApiUrl = "http://172.91.10.189/api/v2/job_templates/9/";
        String accessToken = "6NHpotS8gptsgnbZM2B4yiFQHQq7mz";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = "{\"job_template\": \"9\"}";
        System.out.println(requestBody);

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(ansibleApiUrl, HttpMethod.GET, requestEntity,
                String.class);

        String responseBody = responseEntity.getBody();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        // Extract last job details
        JsonNode lastJob = jsonNode.get("summary_fields").get("last_job");
        int lastJobId = lastJob.get("id").asInt();
        String lastJobStatus = lastJob.get("status").asText();

        // Print the results
        System.out.println("Last Job ID: " + lastJobId);
        System.out.println("Last Job Status: " + lastJobStatus);

        return ("Last Job ID: " + lastJobId + "\n Last Job Status: " + lastJobStatus);
    }
}
