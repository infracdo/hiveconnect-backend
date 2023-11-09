package com.autoprov.autoprov.controllers;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.autoprov.autoprov.controllers.AcsController;
import com.autoprov.autoprov.domain.Client;
import com.autoprov.autoprov.domain.IpAddress;
import com.autoprov.autoprov.repositories.ClientRepository;
import com.autoprov.autoprov.repositories.IpAddressRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.*;

import io.micrometer.core.ipc.http.HttpSender.Response;

@CrossOrigin(origins = "*")
@RestController
public class AutoProvisionController {
    // Insert playbook invokes here
    @Autowired
    private IpAddressRepository ipAddRepo;

    @Autowired
    private ClientRepository clientRepo;

    @Async("AsyncExecutor")
    @GetMapping("/hello")
    public String helloWorld() {
        return "Hi!";
    }

    @Async("AsyncExecutor")
    @PostMapping("/executeProvision")
    public ResponseEntity<Map<String, String>> executeProvision(@RequestBody Map<String, String> params)
            throws JsonMappingException, JsonProcessingException, InterruptedException {

        String networkType = "";
        System.out.println("HiveService: Provision executed");

        // Prepare RequestBody Values
        String accountNo = params.get("accountNo");
        String clientName = params.get("clientName");
        String serialNumber = params.get("serialNumber");
        String macAddress = params.get("macAddress");
        // String cidr = params.get("cidr"); // Cidr block of site
        String site = params.get("site"); // To determine IPAM site
        String oltIp = params.get("olt");
        // String wanMode = params.get("wanMode"); // Bridged or Routed
        String packageType = params.get("packageType");
        String upstream = params.get("upstream");
        String downstream = params.get("downstream");

        // TODO: Dynamic Site, get actual IP Address according to Site
        site = "CDO_1";
        String ipAddress = ipAddRepo
                .getOneAvailableIpAddressUnderSite(site, "Private")
                .get(0)
                .getIpAddress();

        String defaultGateway = ipAddRepo.getGatewayOfIpAddress(ipAddress.substring(0,
                (ipAddress.lastIndexOf("."))));

        // ACS Processes
        Optional<IpAddress> ipAddressData = ipAddRepo.findByipAddress(ipAddress);
        Integer vlanId = ipAddressData.get().getVlanId();

        String acsPushResponse = pushToACS(clientName, serialNumber, defaultGateway,
                ipAddress, vlanId);

        if (acsPushResponse.contains("Successful")) {
            ResponseEntity responseEntity = executeMonitoring(accountNo, serialNumber, macAddress, clientName,
                    ipAddress, packageType, upstream,
                    downstream, oltIp);

            return responseEntity;

        } else {
            AcsController.deleteWanInstance(serialNumber);
            AcsController.rollbackSsid(serialNumber);
            Map<String, String> response = new HashMap<>();
            response.put("Status", "500");
            response.put("Error", acsPushResponse);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
        // return acsPushResponse;

    }

    @Async("AsyncExecutor")
    @PostMapping("/executeAutoConfig")
    public ResponseEntity<Map<String, String>> executeAutoConfig(@RequestBody Map<String, String> params)
            throws JsonMappingException, JsonProcessingException, InterruptedException {

        String networkType = "";
        System.out.println("HiveService: Provision executed");

        // Prepare RequestBody Values
        String accountNo = params.get("accountNo");
        String clientName = params.get("clientName");
        String serialNumber = params.get("serialNumber");
        String macAddress = params.get("macAddress");
        String ipAddress = params.get("ipAddress");
        String site = params.get("site"); // To determine IPAM site
        String oltIp = params.get("olt");
        // String wanMode = params.get("wanMode"); // Bridged or Routed
        String packageType = params.get("packageType");
        String upstream = params.get("upstream");
        String downstream = params.get("downstream");

        // site = "CDO_1";
        // String ipAddress = ipAddRepo
        // .getOneAvailableIpAddressUnderSite(site, "Private")
        // .get(0)
        // .getIpAddress();

        String defaultGateway = ipAddRepo.getGatewayOfIpAddress(ipAddress.substring(0,
                (ipAddress.lastIndexOf("."))));

        // ACS Processes
        Optional<IpAddress> ipAddressData = ipAddRepo.findByipAddress(ipAddress);
        Integer vlanId = ipAddressData.get().getVlanId();

        String acsPushResponse = pushToACS(clientName, serialNumber, defaultGateway,
                ipAddress, vlanId);

        if (acsPushResponse.contains("Successful")) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "200");
            response.put("message", acsPushResponse);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            AcsController.deleteWanInstance(serialNumber);
            AcsController.rollbackSsid(serialNumber);
            Map<String, String> response = new HashMap<>();
            response.put("status", "500");
            response.put("message", acsPushResponse);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
        // return acsPushResponse;

    }

    @Async("AsyncExecutor")
    @PostMapping("/executeMonitoring")
    public ResponseEntity<Map<String, String>> executeMonitoringAPI(@RequestBody Map<String, String> params)
            throws JsonMappingException, JsonProcessingException, InterruptedException {
        String accountNo = params.get("accountNo");
        String clientName = params.get("clientName");
        String serialNumber = params.get("serialNumber");
        String macAddress = params.get("macAddress");
        String ipAddress = params.get("ipAddress");
        String oltIp = params.get("olt");
        String packageType = params.get("packageType");
        String upstream = params.get("upstream");
        String downstream = params.get("downstream");

        return executeMonitoring(accountNo, serialNumber, macAddress, clientName, ipAddress, packageType, upstream,
                downstream, oltIp);

    }

    // AutoProvisioning
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

        return jsonResponse;
    }

    public ResponseEntity<Map<String, String>> executeMonitoring(String accountNo, String serialNumber,
            String macAddress,
            String clientName,
            String onu_private_ip, String packageType, String upstream, String downstream, String oltIp)
            throws JsonMappingException, JsonProcessingException, InterruptedException {

        if (packageType.equals("RES10mbps")) {
            upstream = "15000";
            downstream = "15000";
        }

        String ansibleApiUrl = "http://172.91.10.189/api/v2/job_templates/15/launch/";
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
                "\\npackage_type: " + packageType +
                "\\ndownstream: " + downstream +
                "\\nupstream: " + upstream + "\""
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

            Optional<Client> optionalClient = clientRepo.findClientBySerialNumber(serialNumber);
            if (optionalClient.isPresent()) {
                Client client = optionalClient.get();
                client.setOnuDeviceName(deviceName);
                clientRepo.save(client);
            }

        } else {
            System.out.println("Request failed. Response: " + response.getStatusCode());
            return (ResponseEntity<Map<String, String>>) ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        TimeUnit.SECONDS.sleep(150);

        ResponseEntity lastJobStatus = lastJobStatus();

        if (lastJobStatus.getStatusCode().equals(HttpStatus.OK)) {
            ipAddRepo.associateIpAddressToAccountNumber(accountNo, onu_private_ip);
            AcsController.setInformInterval(serialNumber);
            AcsController.onuOnboarded(serialNumber);
            return lastJobStatus;
        } else {
            AcsController.deleteWanInstance(serialNumber);
            AcsController.rollbackSsid(serialNumber);

            return lastJobStatus;
        }
    }

    // Troubleshooting
    @Async("AsyncExecutor")
    @GetMapping("/lastJobStatus")
    public ResponseEntity<Map<String, String>> lastJobStatus()
            throws JsonMappingException, JsonProcessingException, InterruptedException {

        String ansibleApiUrl = "http://172.91.10.189/api/v2/job_templates/15/";
        String accessToken = "6NHpotS8gptsgnbZM2B4yiFQHQq7mz";
        String error = "";

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
        JsonNode lastJobId = lastJob.get("id");
        String lastJobStatus = lastJob.get("status").asText();

        // Print the results
        System.out.println("Job ID: " + lastJobId);
        System.out.println("Job Status: " + lastJobStatus);

        if (lastJobStatus.contains("fail")) {

            ansibleApiUrl = "http://172.91.10.189/api/v2/jobs/" + lastJobId + "/job_events/?failed=True";
            requestEntity = new HttpEntity<>(requestBody, headers);

            restTemplate = new RestTemplate();
            responseEntity = restTemplate.exchange(ansibleApiUrl, HttpMethod.GET, requestEntity,
                    String.class);
            String stderr = responseEntity.getBody().toString();

            System.out.println(responseBody);

            try {

                if (stderr.contains("Pseudo-terminal will not be allocated because stdin is not a terminal"))
                    error = "Bad OLT-IP";

                if (stderr.contains("name: OLT Vendor"))
                    error = "Bad OLT-IP; OLT-IP not live";

                if (stderr.contains("Host with the same visible name"))
                    error = "Client's device is already provisioned";

                if (stderr.contains("Duplicate termination found"))
                    error = "IP Address already assigned to someone";

                if (stderr.contains("[prometheus]: UNREACHABLE! =>"))
                    error = "Monitoring platform Prometheus is unreachable. Try again later";

                if (stderr.contains("FAILED!") && stderr.contains("mac-address-table"))
                    error = "Error on MAC Address Filtering";

                System.out.println("stderr: " + stderr);

                Map<String, String> response = new HashMap<>();
                response.put("status", "500");
                response.put("message", error);
                response.put("awx_job_id: ", lastJobId.toString());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

            }

            catch (

            Exception e) {
                e.printStackTrace();
            }
        }

        // return ("Job ID: " + lastJobId + "\nStatus: " + lastJobStatus + error);
        Map<String, String> response = new HashMap<>();
        response.put("status", "200");
        response.put("message", "Provisioning and Monitoring Successful!");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    public String getAvailableIpAddress(String type, String cidr) {
        String ipAddress = ipAddRepo
                .getOneAvailableIpAddressUnderCidrBlockAndType("Private",
                        (cidr.substring(0, (cidr.lastIndexOf(".")))))
                .get(0)
                .getIpAddress();
        String defaultGateway = ipAddRepo.getGatewayOfIpAddress(cidr.substring(0, (cidr.lastIndexOf("."))));

        return ipAddress;
    }
}

// IP Address Assignment - Block
// String ipAddress = ipAddRepo
// .getOneAvailableIpAddressUnderCidrBlockAndType("Private",
// (cidr.substring(0, (cidr.lastIndexOf(".")))))
// .get(0)
// .getIpAddress();

// IP Address Assignment - BlockAndType
// String ipAddress = ipAddRepo
// .getOneAvailableIpAddressUnderCidrBlockAndType(networkType,
// (cidr.substring(0, (cidr.lastIndexOf(".")))))
// .get(0)
// .getIpAddress(); // TODO: change according to request. Should be able to
// provide both private and
// // public if needed

// TODO: shift api to receive number of private and public IP required

// if (packageType.contains("RES"))
// networkType = "Private";

// if (packageType.contains("SME"))
// networkType = "Public";