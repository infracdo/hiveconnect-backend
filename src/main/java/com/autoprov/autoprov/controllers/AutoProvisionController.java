package com.autoprov.autoprov.controllers;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.autoprov.autoprov.controllers.AcsController;
import com.autoprov.autoprov.entity.hiveDomain.HiveClient;
import com.autoprov.autoprov.entity.inetDomain.Client;
import com.autoprov.autoprov.entity.inetDomain.ClientDetail;
import com.autoprov.autoprov.entity.inetDomain.PackageType;
import com.autoprov.autoprov.entity.ipamDomain.IpAddress;
import com.autoprov.autoprov.repositories.acsRepositories.DeviceRepository;
import com.autoprov.autoprov.repositories.acsRepositories.DevicesRepository;
import com.autoprov.autoprov.repositories.hiveRepositories.HiveClientRepository;
import com.autoprov.autoprov.repositories.inetRepositories.ClientDetailRepository;
import com.autoprov.autoprov.repositories.inetRepositories.ClientRepository;
import com.autoprov.autoprov.repositories.inetRepositories.PackageRepository;
import com.autoprov.autoprov.repositories.ipamRepositories.IpAddressRepository;
import com.autoprov.autoprov.services.HiveClientService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.micrometer.core.ipc.http.HttpSender.Response;

@CrossOrigin(origins = "*")
@RestController
public class AutoProvisionController {
    // Insert playbook invokes here

    private Boolean showBody = false;

    private static String playbookMonitoringApiUrl = "http://172.91.10.189/api/v2/job_templates/15/";
    private static String playbookPreProvUrl = "http://172.91.10.189/api/v2/job_templates/18/";
    private static String playbookGetJobUrl = "http://172.91.10.189/api/v2/jobs/";
    private static String acsApiUrl = "http://172.91.0.136:7547/";

    @Autowired
    private IpAddressRepository ipAddRepo;

    @Autowired
    private ClientRepository clientRepo;

    @Autowired
    private ClientDetailRepository clientDetailRepo;

    @Autowired
    private HiveClientRepository hiveClientRepo;

    @Autowired
    private PackageRepository packageRepo;

    @Autowired
    private DevicesRepository devicesRepo;

    @Autowired
    private DeviceRepository deviceRepo;

    // General Exposed Endpoints ----------------------------
    @Async("AsyncExecutor")
    @GetMapping("/hello")
    public String helloWorld() {
        return "Hi!";
    }

    // General Exposed Endpoints ----------------------------

    // API for INET ----------------------------------------------
    @Async("AsyncExecutor")
    @PostMapping("/executeProvision")
    public ResponseEntity<Map<String, String>> executeInetProvision(@RequestBody Map<String, String> params)
            throws JsonMappingException, JsonProcessingException, InterruptedException {

        String networkType = "";
        System.out.println(">>> HiveService: Provision executed");

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
        String vlanId = ipAddressData.get().getVlanId();

        String acsPushResponse = executeInetAutoProv(accountNo, clientName, serialNumber, defaultGateway,
                ipAddress, vlanId);

        if (acsPushResponse.contains("Successful")) { // TODO: revert to monitoring for INET
            ResponseEntity responseEntity = executeInetMonitoring(accountNo, serialNumber, macAddress, clientName,
                    ipAddress, packageType, upstream,
                    downstream, oltIp);

            return responseEntity;

        } else {
            // AcsController.deleteWanInstance(serialNumber);
            AcsController.rollbackSsid(serialNumber);
            Map<String, String> response = new HashMap<>();
            response.put("Status", "500");
            response.put("Error", acsPushResponse);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
        // return acsPushResponse;

    }

    public ResponseEntity<Map<String, String>> executeInetMonitoring(String accountNo, String serialNumber,
            String macAddress,
            String clientName,
            String ipAddress, String packageType, String upstream, String downstream, String oltIp)
            throws JsonMappingException, JsonProcessingException, InterruptedException {

        if (packageType.equals("RES10mbps")) {
            upstream = "15000";
            downstream = "15000";
        }

        String packageName = "";
        Optional<PackageType> optionalPackage = packageRepo.findBypackageId(packageType);
        if (optionalPackage.isPresent()) {
            PackageType packageT = optionalPackage.get();

            if (showBody)
                System.out.println(packageT.toString());

            packageName = packageT.getName();

        }

        String ansibleApiUrl = playbookMonitoringApiUrl + "launch/";
        String accessToken = "6NHpotS8gptsgnbZM2B4yiFQHQq7mz";

        String deviceName = "" + clientName.replace(" ", "_") + "_bw1";

        if (showBody)
            System.out.println(deviceName);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        TimeUnit.SECONDS.sleep(20);
        AcsController.getWan2MacAddress(serialNumber);
        TimeUnit.SECONDS.sleep(20);

        String requestBody = "{\n" +
                "\"job_template\": \"15\",\n" +
                "\"ask_variables_on_launch\": \"true\",\n" +
                "\"extra_vars\": \"---" +
                "\\nserial_number: " + serialNumber +
                "\\ndevice_name: " + deviceName +
                "\\nmac_address: " + macAddress +
                "\\nolt_ip: " + oltIp +
                "\\naccount_number: " + accountNo + // TODO: add actual account number
                "\\nstatus: Activated " +
                "\\nprovisioned_by: HiveConnect " +
                "\\nvlan_690_ip: " + devicesRepo.getOnuInfoBySerialNumber(serialNumber).get(0).getPublicIp() +
                "\\nvlan_2010_mac: " + devicesRepo.getOnuInfoBySerialNumber(serialNumber).get(0).getSecondWanMac() +
                "\\nonu_private_ip: " + ipAddress +
                "\\npackage_type: " + packageName +
                "\\ndownstream: " + downstream +
                "\\nupstream: " + upstream + "\""
                +
                "}";
        if (showBody)
            System.out.println(requestBody);

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(ansibleApiUrl,
                HttpMethod.POST, requestEntity,
                String.class);

        System.out.println(">>> HiveConnect: Ansible executed");
        String jobId;
        if (response.getStatusCode() == HttpStatus.CREATED) {
            System.out.println("Request successful.");
            if (showBody)
                System.out.println(response.getBody());

            String responseBody = response.getBody();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            jobId = jsonNode.get("id").asText();

        } else {
            System.out.println("Request failed. Response: " + response.getStatusCode());
            if (showBody)
                System.out.println(response.getBody());
            return (ResponseEntity<Map<String, String>>) ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        ResponseEntity lastJobStatus = lastJobStatus(clientName, jobId);

        if (lastJobStatus.getStatusCode().equals(HttpStatus.OK)) {
            // finalize and mark everything to be activated
            ipAddRepo.associateIpAddressToAccountNumber(accountNo, ipAddress);
            AcsController.setInformIntervalPostProv(serialNumber);
            AcsController.onuOnboarded(serialNumber);

            String ssidName = clientName.replace(" ", "_");

            String oltInterface = getOltInterface(jobId);

            String[] bandwidth = getOltBandwidth(jobId);

            Optional<Client> optionalClient = clientRepo.findByAccountNumber(accountNo);
            if (optionalClient.isPresent()) {
                Client client = optionalClient.get();
                client.setOnuDeviceName(deviceName);
                client.setOnuMacAddress(macAddress);
                client.setStatus("Activated");
                client.setIpAssigned(ipAddress);
                client.setBucketId("100");
                clientRepo.save(client);

                HiveClientService.addHiveNewClient(accountNo, client.getClientName(), serialNumber, deviceName,
                        macAddress, oltIp, oltInterface,
                        ipAddress,
                        ssidName, packageType, bandwidth[0], bandwidth[1]);

                deviceRepo.updateParentBySerialNumber("Hive Test", serialNumber);
            }

            return lastJobStatus;
        } else {
            AcsController.deleteWanInstance(serialNumber);
            AcsController.rollbackSsid(serialNumber);

            return lastJobStatus;
        }
    }

    public String executeInetAutoProv(String accountNumber, String clientName, String serialNumber,
            String defaultGateway,
            String ipAddress,
            String vlanId) {
        // Define the API URL
        String apiUrl = acsApiUrl + "executeAutoConfig";

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
        if (showBody)
            System.out.println(jsonRequestBody);
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        String jsonResponse = restTemplate.postForObject(apiUrl, requestEntity, String.class);

        System.out.println(">>> HiveConnect: ACS Push executed");
        System.out.println("Response: " + jsonResponse);

        return jsonResponse;
    }
    // API for INET (end) ----------------------------------------------

    // APIs for HiveApp ----------------------------------------------
    @Async("AsyncExecutor")
    @PostMapping("/executeAutoConfig")
    public ResponseEntity<Map<String, String>> executeHiveAutoConfig(@RequestBody Map<String, String> params)
            throws JsonMappingException, JsonProcessingException, InterruptedException {

        String networkType = "";
        System.out.println(">>> HiveService: Provision executed from HiveApp");

        // Prepare RequestBody Values
        String accountNo = params.get("accountNo");
        String clientName = params.get("clientName");
        String serialNumber = params.get("serialNumber");
        String macAddress = params.get("macAddress");
        String site = params.get("site"); // To determine IPAM site
        String oltIp = params.get("olt");
        // String wanMode = params.get("wanMode"); // Bridged or Routed
        String packageType = params.get("packageType");
        String upstream = params.get("upstream");
        String downstream = params.get("downstream");

        site = "CDO_1";
        // String ipAddress = ipAddRepo
        // .getOneAvailableIpAddressUnderSite(site, "Private")
        // .get(0)
        // .getIpAddress();

        String ipAddress = ipAddRepo
                .getOneAvailableIpAddressUnderSite(site, "Private")
                .get(0)
                .getIpAddress();

        String defaultGateway = ipAddRepo.getGatewayOfIpAddress(ipAddress.substring(0,
                (ipAddress.lastIndexOf("."))));

        // ACS Processes
        Optional<IpAddress> ipAddressData = ipAddRepo.findByipAddress(ipAddress);
        String vlanId = ipAddressData.get().getVlanId();

        String acsResponse = executeInetAutoProv(accountNo, clientName, serialNumber, defaultGateway,
                ipAddress, vlanId);

        if (acsResponse.contains("Successful")) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "200");
            response.put("message", acsResponse);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            AcsController.deleteWanInstance(serialNumber);
            AcsController.rollbackSsid(serialNumber);
            Map<String, String> response = new HashMap<>();
            response.put("status", "500");
            response.put("message", acsResponse);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
        // return acsPushResponse;

    }

    // APIs for HiveApp (end) ----------------------------------------------

    // AutoProvisioning

    @Async("AsyncExecutor")
    @PostMapping("/executeMonitoring")
    public ResponseEntity<Map<String, String>> executeHiveMonitoring(@RequestBody Map<String, String> params)
            throws JsonMappingException, JsonProcessingException, InterruptedException {
        String accountNo = params.get("accountNo");
        String clientName = params.get("clientName");
        String serialNumber = params.get("serialNumber");
        String macAddress = params.get("macAddress");
        String ipAddress = ipAddRepo
                .getOneAvailableIpAddressUnderSite("CDO_1", "Private")
                .get(0)
                .getIpAddress();

        if (showBody)
            System.out.println(ipAddRepo
                    .getOneAvailableIpAddressUnderSite("CDO_1", "Private"));
        String oltIp = params.get("olt");
        String packageType = params.get("packageType");
        String upstream = params.get("upstream");
        String downstream = params.get("downstream");
        String packageName = "";

        Optional<PackageType> optionalPackage = packageRepo.findBypackageId(packageType);
        if (optionalPackage.isPresent()) {
            PackageType packageT = optionalPackage.get();
            if (showBody)
                System.out.println(packageT.toString());
            upstream = packageT.getUpstream();
            downstream = packageT.getDownstream();
            packageName = packageT.getName();

        }

        String ansibleApiUrl = playbookMonitoringApiUrl + "launch/";
        String accessToken = "6NHpotS8gptsgnbZM2B4yiFQHQq7mz";

        String deviceName = "" + clientName.replace(" ", "_") + "_bw1";
        if (showBody)
            System.out.println(deviceName);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        TimeUnit.SECONDS.sleep(20);
        AcsController.getWan2MacAddress(serialNumber);
        TimeUnit.SECONDS.sleep(20);

        String requestBody = "{\n" +
                "\"job_template\": \"15\",\n" +
                "\"ask_variables_on_launch\": \"true\",\n" +
                "\"extra_vars\": \"---" +
                "\\nserial_number: " + serialNumber +
                "\\ndevice_name: " + deviceName +
                "\\nmac_address: " + macAddress +
                "\\nolt_ip: " + oltIp +
                "\\naccount_number: " + accountNo + // TODO: add actual account number
                "\\nstatus: Activated " +
                "\\nprovisioned_by: HiveConnect " +
                "\\nvlan_690_ip: " + devicesRepo.getOnuInfoBySerialNumber(serialNumber).get(0).getPublicIp() +
                "\\nvlan_2010_mac: " + devicesRepo.getOnuInfoBySerialNumber(serialNumber).get(0).getSecondWanMac() +
                "\\nonu_private_ip: " + ipAddress +
                "\\npackage_type: " + packageName +
                "\\ndownstream: " + downstream +
                "\\nupstream: " + upstream + "\""
                +
                "}";
        if (showBody)
            System.out.println(requestBody);

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(ansibleApiUrl,
                HttpMethod.POST, requestEntity,
                String.class);

        System.out.println(">>> HiveConnect: Ansible executed");
        String jobId;
        if (response.getStatusCode() == HttpStatus.CREATED) {
            System.out.println("Request successful.");
            if (showBody)
                System.out.println(response.getBody());

            String responseBody = response.getBody();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            jobId = jsonNode.get("id").asText();

        } else {
            System.out.println("Request failed. Response: " + response.getStatusCode());
            if (showBody)
                System.out.println(response.getBody());
            return (ResponseEntity<Map<String, String>>) ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        ResponseEntity lastJobStatus = lastJobStatus(clientName, jobId);

        if (lastJobStatus.getStatusCode().equals(HttpStatus.OK)) {
            // finalize and mark everything to be activated
            ipAddRepo.associateIpAddressToAccountNumber(accountNo, ipAddress);
            AcsController.setInformIntervalPostProv(serialNumber);
            AcsController.onuOnboarded(serialNumber);

            String ssidName = clientName.replace(" ", "_");

            String oltInterface = getOltInterface(jobId);
            String[] bandwidth = getOltBandwidth(jobId);

            Optional<Client> optionalClient = clientRepo.findByAccountNumber(accountNo);
            if (optionalClient.isPresent()) {
                Client client = optionalClient.get();
                client.setOnuDeviceName(deviceName);
                client.setOnuMacAddress(macAddress);
                client.setStatus("Activated");
                client.setIpAssigned(ipAddress);
                client.setBucketId("100");
                clientRepo.save(client);

                HiveClientService.addHiveNewClient(accountNo, client.getClientName(), serialNumber, deviceName,
                        macAddress, oltIp, oltInterface,
                        ipAddress,
                        ssidName, packageType, bandwidth[0], bandwidth[1]);

                // Optional<ClientDetail> optionalClientDetail =
                // clientDetailRepo.findByClientId(client.getId());
                // if (optionalClientDetail.isPresent()) {
                // ClientDetail clientDetail = optionalClientDetail.get();
                // clientDetail.setStatus("finished");

                // }

                deviceRepo.updateParentBySerialNumber("Hive Test", serialNumber);
            }

            return lastJobStatus;
        } else {
            AcsController.deleteWanInstance(serialNumber);
            AcsController.rollbackSsid(serialNumber);

            return lastJobStatus;
        }

        // [[[[[[[------ALL GREEN TEST------]]]]]]] ------------------------------

        // String ssidName = clientName.replace(" ", "_") + " 2.4/5G";
        // String password = "" + ssidName + "1234";

        // ipAddRepo.associateIpAddressToAccountNumber(accountNo, ipAddress);

        // Optional<Client> optionalClient = clientRepo.findByAccountNumber(accountNo);
        // if (optionalClient.isPresent()) {
        // Client client = optionalClient.get();
        // client.setOnuDeviceName(deviceName);
        // client.setOnuSerialNumber(serialNumber);
        // client.setOnuMacAddress(macAddress);
        // client.setStatus("Activated");
        // client.setIpAssigned(ipAddress);
        // client.setOltIp(oltIp);
        // client.setBackend("HiveConnect");
        // client.setSsidName(ssidName + " 2.4/5G");
        // client.setSsidPw(password);
        // clientRepo.save(client);
        // }

        // Map<String, String> response = new HashMap<>();
        // response.put("status", "200");
        // response.put("message", "Provisioning and Monitoring Successful!");
        // response.put("ssid_name", ssidName + " 2.4G/5G");
        // response.put("ssid_pw", password);

        // return ResponseEntity.status(HttpStatus.OK).body(response);

        // [[[[[[[------ALL GREEN TEST------]]]]]]] ------------------------------
    }

    @Async("AsyncExecutor")
    @PostMapping("/preprovisionCheck")
    public ResponseEntity<Map<String, String>> preprovisionCheck(@RequestBody Map<String, String> params)
            throws InterruptedException, JsonMappingException, JsonProcessingException {

        String jobId;

        System.out.println(">>> HiveService: Pre-Provision Check Initialized");

        String accountNo = params.get("accountNo");
        String clientName = params.get("clientName");
        String serialNumber = params.get("serialNumber");
        String macAddress = params.get("macAddress");
        String ipAddress = ipAddRepo
                .getOneAvailableIpAddressUnderSite("CDO_1", "Private")
                .get(0)
                .getIpAddress();
        String oltIp = params.get("olt");
        String packageType = params.get("packageType");
        String upstream = params.get("upstream");
        String downstream = params.get("downstream");

        String ansibleApiUrl = playbookPreProvUrl + "launch/";
        String accessToken = "6NHpotS8gptsgnbZM2B4yiFQHQq7mz";

        String deviceName = "" + clientName.replace(" ", "_") + "_bw1";

        if (showBody)
            System.out.println(deviceName);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = "{\n" +
                "\"job_template\": \"18\",\n" +
                "\"ask_variables_on_launch\": \"true\",\n" +
                "\"extra_vars\": \"---" +
                "\\nserial_number: " + serialNumber +
                "\\ndevice_name: " + deviceName +
                "\\nmac_address: " + macAddress +
                "\\nolt_ip: " + oltIp +
                "\\naccount_number: " + accountNo + // TODO: add actual account number
                "\\nstatus: Activated " +
                "\\nonu_private_ip: " + ipAddress +
                "\\npackage_type: " + packageType +
                "\\ndownstream: " + downstream +
                "\\nupstream: " + upstream + "\""
                +
                "}";
        if (showBody)
            System.out.println(requestBody);

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(ansibleApiUrl, HttpMethod.POST, requestEntity,
                String.class);

        String responseBody = responseEntity.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        jobId = jsonNode.get("id").asText();

        if (showBody)
            System.out.println(responseBody); // TODO: retrieve all string because limited string is printed
        System.out.println("Checking Job Id " + jobId);

        ansibleApiUrl = "" + playbookGetJobUrl + jobId + "/stdout";
        requestEntity = new HttpEntity<>(requestBody, headers);

        restTemplate = new RestTemplate();
        String checkingResponse = null;
        StringBuilder tries = new StringBuilder();
        System.out.println("Trying Get Job " + jobId);
        while (checkingResponse == null || !checkingResponse.contains("PLAY RECAP")) {

            TimeUnit.SECONDS.sleep(10);
            responseEntity = restTemplate.exchange(ansibleApiUrl, HttpMethod.GET, requestEntity,
                    String.class);

            checkingResponse = responseEntity.getBody();

            if (checkingResponse == null || !checkingResponse.contains("PLAY RECAP")) {

                tries.append("|");
                System.out.println(tries.toString());
                continue;
            }
        }

        if (showBody)
            System.out.println(checkingResponse);
        StringBuilder errors = new StringBuilder();
        Boolean errorExisting = false;

        String onuCheckString = "ONU exist in " + oltIp + "";
        String onuCheckStringAlt = "ONU exist in '" + oltIp + "'";
        String subscriberCheckString = "Subscriber '" + deviceName + "' is not yet onboarded";
        String ipAddressCheckString = "IP Address '" + ipAddress + "' is not yet onboarded";

        String wrongOnuString = "Wrong OLT Selected";
        String subscriberExistsString = "Subscriber '" + deviceName + "' already exist in Netbox";
        String ipAddressExistsString = "IP Address '" + ipAddress + " ' already exist in Netbox";

        if (checkingResponse.contains("PLAY RECAP")) {
            if (checkingResponse.contains(onuCheckString) || checkingResponse.contains(onuCheckStringAlt))
                System.out.println("Onu OK");
            else {
                errors.append("Wrong OLT selected.");
                errorExisting = true;
            }

            if (checkingResponse.contains(subscriberCheckString))
                System.out.println("subscriber OK");
            else {
                errors.append("Subscriber Exists.");
                errorExisting = true;
            }

            if (checkingResponse.contains(ipAddressCheckString))
                System.out.println("ip OK");
            else {
                errors.append("IP Address conflict.");
                errorExisting = true;
            }

            if (!errorExisting) {
                Map<String, String> response = new HashMap<>();
                response.put("status", "200");
                response.put("message", "All Clear. Proceed to Provisioning!");
                response.put("body", checkingResponse);
                return ResponseEntity.status(HttpStatus.OK).body(response);

            }

            else {

                Map<String, String> response = new HashMap<>();
                response.put("status", "500");
                response.put("message", errors.toString());
                response.put("body", checkingResponse);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        }

        Map<String, String> response = new HashMap<>();
        response.put("status", "500");
        response.put("message", "No Result");
        response.put("body", checkingResponse);
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

    // Troubleshooting
    @Async("AsyncExecutor")
    @GetMapping("/lastJobStatus")
    public ResponseEntity<Map<String, String>> lastJobStatus(String clientName, String jobId)
            throws JsonMappingException, JsonProcessingException, InterruptedException {

        String ansibleApiUrl = playbookGetJobUrl + jobId;
        String accessToken = "6NHpotS8gptsgnbZM2B4yiFQHQq7mz";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = "";
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = null;
        String responseBody = null;

        StringBuilder tries = new StringBuilder();

        System.out.println("Trying Get Job " + jobId);
        while (responseBody == null || responseBody.contains("\"finished\":null")) {
            TimeUnit.SECONDS.sleep(10);
            responseEntity = restTemplate.exchange(ansibleApiUrl, HttpMethod.GET, requestEntity,
                    String.class);

            if (responseEntity.getStatusCode() == HttpStatus.NOT_FOUND) {

                tries.append("|");
                System.out.println(tries.toString());
                continue;
            }
            responseBody = responseEntity.getBody();
            if (responseBody == null || responseBody.contains("\"finished\":null")) {
                tries.append("|");
                System.out.println(tries.toString());
                continue;
            }
        }

        if (showBody)
            System.out.println(responseBody);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        // Extract last job details
        String lastJobStatus = jsonNode.get("status").asText();

        // Print the results
        System.out.println("Job ID: " + jobId);
        System.out.println("Job Status: " + lastJobStatus);

        if (lastJobStatus.contains("fail")) {

            ansibleApiUrl = "" + playbookGetJobUrl + jobId + "/job_events/?failed=True";
            requestEntity = new HttpEntity<>(requestBody, headers);

            restTemplate = new RestTemplate();
            responseEntity = restTemplate.exchange(ansibleApiUrl, HttpMethod.GET, requestEntity,
                    String.class);
            String stderr = responseEntity.getBody().toString();

            if (showBody)
                System.out.println(responseBody);
            StringBuilder error = new StringBuilder();

            try {

                if (stderr.contains("Pseudo-terminal will not be allocated because stdin is not a terminal"))
                    error.append("Bad OLT-IP.");

                if (stderr.contains("name: OLT Vendor"))
                    error.append("Bad OLT-IP; OLT-IP not live.");

                if (stderr.contains("Host with the same visible name"))
                    error.append("Client's device is already provisioned.");

                if (stderr.contains("UnboundLocalError: local variable 'name' referenced before assignment"))
                    error.append("Device on the OLT Interface already provisioned.");

                if (stderr.contains("Duplicate termination found"))
                    error.append("IP Address already assigned to someone.");

                if (stderr.contains("[prometheus]: UNREACHABLE! =>"))
                    error.append("Monitoring platform Prometheus is unreachable. Try again later.");

                if (stderr.contains("FAILED!") && stderr.contains("mac-address-table"))
                    error.append("Error on MAC Address Filtering.");

                System.out.println("Errors: " + stderr);

                Map<String, String> response = new HashMap<>();
                response.put("status", "500");
                response.put("message", error.toString());
                response.put("awx_job_id: ", jobId.toString());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

            }

            catch (

            Exception e) {
                e.printStackTrace();
            }
        }

        ansibleApiUrl = "" + playbookGetJobUrl + jobId + "/stdout";
        requestEntity = new HttpEntity<>(requestBody, headers);

        restTemplate = new RestTemplate();
        responseEntity = restTemplate.exchange(ansibleApiUrl, HttpMethod.GET, requestEntity,
                String.class);

        responseBody = responseEntity.getBody();

        // Define the pattern
        Pattern pattern = Pattern.compile("\"olt_interface_bind\\.stdout\"\\s*:\\s*\"([^\"]+)\"");

        // Create a matcher
        Matcher matcher = pattern.matcher(responseBody);

        // Find the match
        if (matcher.find()) {
            // Extract the desired value
            String oltInterfaceBind = matcher.group(1);
            System.out.println("olt_interface_bind.stdout: " + oltInterfaceBind);
        } else {
            System.out.println("Match not found");
        }

        String newSsid = clientName.replace(" ", "_");
        String password = "" + clientName + "1234";

        // return ("Job ID: " + jobId + "\nStatus: " + lastJobStatus + error);
        Map<String, String> response = new HashMap<>();
        response.put("awx_job_id", jobId);
        response.put("status", "200");
        response.put("message", "Provisioning and Monitoring Successful!");
        response.put("ssid_name", newSsid + "2.4G/5G");
        response.put("ssid_pw", password);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // Get OLT Interface
    @Async("AsyncExecutor")
    @GetMapping("/getOltInterface")
    public String getOltInterface(String jobId) {

        String ansibleApiUrl = "" + playbookGetJobUrl + jobId + "/stdout";
        String accessToken = "6NHpotS8gptsgnbZM2B4yiFQHQq7mz";
        String error = "";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = "";
        HttpEntity requestEntity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(ansibleApiUrl, HttpMethod.GET, requestEntity,
                String.class);

        String responseBody = responseEntity.getBody();

        // ------------Guangda OLT Interface Check
        System.out.println("OLT Interface Check: Guangda");
        Pattern guangdaInterfaceBindPattern = Pattern.compile("\"olt_interface_bind\\.stdout\"\\s*:\\s*\"([^\"]+)\"");

        // Create a matcher
        Matcher guangdaMatcher = guangdaInterfaceBindPattern.matcher(responseBody);

        // Find the match
        if (guangdaMatcher.find()) {
            // Extract the desired value
            String guangdaOltInterface = guangdaMatcher.group(1);
            System.out.println("Guangda olt_interface_bind.stdout: " + guangdaOltInterface);
            return guangdaOltInterface;
        } else {
            System.out.println("OLT Interface Check: Guangda OLT Interface Match not found");
        }

        System.out.println(responseBody);
        Pattern pattern = Pattern.compile("\"stdout\": \"(.*?)\"");

        // Create a matcher with the input string
        Matcher matcher = pattern.matcher(responseBody);

        // Find the first match
        if (matcher.find()) {
            // Extract the EPON value
            String vsolOltInterface = matcher.group(1);
            System.out.println("EPON Value: " + vsolOltInterface);
            return vsolOltInterface;
        } else {
            System.out.println("OLT Interface Check: VSOL OLT Interface Match not found");
        }

        return "No OLT Interface found";

    }

    @Async("AsyncExecutor")
    @GetMapping("/getOltBandwidth")
    public String[] getOltBandwidth(String jobId) {

        String ansibleApiUrl = "" + playbookGetJobUrl + "1424" + "/stdout";
        String accessToken = "6NHpotS8gptsgnbZM2B4yiFQHQq7mz";
        String error = "";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = "";
        HttpEntity requestEntity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(ansibleApiUrl, HttpMethod.GET, requestEntity,
                String.class);

        String responseBody = responseEntity.getBody();
        System.out.println(responseBody);

        String upstreamValue = "";
        String downstreamValue = "";

        // Pattern pattern = Pattern.compile("\"msg\".*?\"Upstream\":
        // \"(\\d+\\.\\d+)\".*?\"Downstream\": \"(\\d+\\.\\d+)\"");
        // Matcher matcher = pattern.matcher(responseBody);

        // while (matcher.find()) {
        // upstreamValue = matcher.group(1);
        // downstreamValue = matcher.group(2);

        // System.out.println("Upstream: " + upstreamValue);
        // System.out.println("Downstream: " + downstreamValue);
        // }
        ObjectMapper objectMapper = new ObjectMapper();

        upstreamValue = parseValue(responseBody, "Upstream");
        downstreamValue = parseValue(responseBody, "Downstream");

        System.out.println("Upstream Value: " + upstreamValue);
        System.out.println("Downstream Value: " + downstreamValue);

        String[] bandwidth = new String[2];
        bandwidth[0] = upstreamValue;
        bandwidth[1] = downstreamValue;

        return bandwidth;

    }

    // Simulate error
    @Async("AsyncExecutor")
    @PostMapping("/simulateHiveMonitoringError")
    public ResponseEntity<Map<String, String>> simulateError(String jobId) {
        Map<String, String> response = new HashMap<>();
        response.put("awx_job_id", jobId);
        response.put("status", "500");
        response.put("message", "Error on Mac Address Filtering!");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @Async("asyncExecutor")
    @PostMapping("/resetHiveDummy")
    public String deleteClient() {
        clientRepo.resetHiveDummy();
        deviceRepo.resetHiveDummy();
        return "Hive Demo Dummy Accounts cleared! Test Devices reverted to rogue!";
    }

    // --------- OTHER FUNCTIONS ------------
    public String getDate() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = currentDate.format(formatter);
        return formattedDate;
    }

    private String parseValue(String input, String keyword) {
        String patternString = keyword + "\": \"([^\"]+)\"";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "Value not found";
        }
    }
    // ------------------------- TEST AREA ------------------------

    @Async("AsyncExecutor")
    @GetMapping("/getOltInterface/{jobId}")
    public String testGetOltInterface(@PathVariable("jobId") String jobId) {
        return getOltInterface(jobId);
    }

    @Async("asyncExecutor")
    @GetMapping("/testExecuteMonitoring")
    public String testExecuteMonitoring(@RequestBody Map<String, String> params) {

        String accountNo = params.get("accountNo");
        String clientName = params.get("clientName");
        String serialNumber = params.get("serialNumber");
        String macAddress = params.get("macAddress");
        String ipAddress = ipAddRepo
                .getOneAvailableIpAddressUnderSite("CDO_1", "Private")
                .get(0)
                .getIpAddress();
        String oltIp = params.get("olt");
        String packageType = params.get("packageType");
        String upstream = params.get("upstream");
        String downstream = params.get("downstream");

        String deviceName = "" + clientName.replace(" ", "_") + "_bw1";

        String ssidName = clientName.replace(" ", "_");

        String oltInterface = getOltInterface("1424");

        String[] bandwidth = getOltBandwidth("1424");

        Optional<Client> optionalClient = clientRepo.findByAccountNumber(accountNo);
        if (optionalClient.isPresent()) {
            Client client = optionalClient.get();
            client.setOnuDeviceName(deviceName);
            client.setOnuMacAddress(macAddress);
            client.setStatus("Activated");
            client.setIpAssigned(ipAddress);
            client.setBucketId("100");
            clientRepo.save(client);

            HiveClientService.addHiveNewClient(accountNo, client.getClientName(), serialNumber, deviceName,
                    macAddress, oltIp, oltInterface,
                    ipAddress,
                    ssidName, packageType, bandwidth[0], bandwidth[1]);

            // Optional<ClientDetail> optionalClientDetail =
            // clientDetailRepo.findByClientId(client.getId());
            // if (optionalClientDetail.isPresent()) {
            // ClientDetail clientDetail = optionalClientDetail.get();
            // clientDetail.setStatus("finished");

            // }

        }
        return "Check database";
    }

}
