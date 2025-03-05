package com.autoprov.autoprov.controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.autoprov.autoprov.entity.hiveDomain.HiveClient;
import com.autoprov.autoprov.entity.ipamDomain.CidrIpAddress;
import com.autoprov.autoprov.entity.subscriberDomain.PackageTypeEntity;
import com.autoprov.autoprov.entity.subscriberDomain.subscriberEntity;
import com.autoprov.autoprov.repositories.acsRepositories.DeviceRepository;
import com.autoprov.autoprov.repositories.acsRepositories.DevicesRepository;
import com.autoprov.autoprov.repositories.hiveRepositories.HiveClientRepository;
import com.autoprov.autoprov.repositories.ipamRepositories.CidrIpAddressRepository;
import com.autoprov.autoprov.repositories.oltRepositories.oltRepository;
import com.autoprov.autoprov.repositories.subscriberRepositories.PackageRepository;
import com.autoprov.autoprov.repositories.subscriberRepositories.subscriberRepository;
import com.autoprov.autoprov.security.jwt.JwtUtils;
import com.autoprov.autoprov.services.HiveClientService;
import com.autoprov.autoprov.services.LogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

@PropertySource("classpath:application.properties")
@CrossOrigin(origins = "*")
@RestController
public class AutoProvisionController {
    // Insert playbook invokes here

    private Boolean showBody = true;

    @Value("${playbookBandwidthLimitationApiUrl}")
    private String playbookBandwidthLimitationApiUrl;

    @Value("${playbookMonitoringApiUrl}")
    private String playbookMonitoringApiUrl;

    @Value("${playbookPreProvUrl}")
    private String playbookPreProvUrl;

    @Value("${playbookGetJobUrl}")
    private String playbookGetJobUrl;

    @Value("${playbookMigrationUrl}")
    private String playbookMigrationUrl;

    @Value("${acsApiUrl}")
    private String acsApiUrl;

    @Value("${ansibleAccessToken}")
    private String ansibleAccessToken;

    @Value("${ansibleMigrationToken}")
    private String ansibleMigrationToken;

    @Autowired
    private CidrIpAddressRepository ipAddRepo;

    @Autowired
    private subscriberRepository clientRepo;

    @Autowired
    private oltRepository oltRepo;
    // @Autowired
    // private ClientDetailRepository clientDetailRepo;

    // @Autowired
    // private HiveClientRepository hiveClientRepo;

    @Autowired
    private PackageRepository packageRepo;

    @Autowired
    private DevicesRepository devicesRepo;

    @Autowired
    private DeviceRepository deviceRepo;

    @Autowired
    private HiveClientRepository hiveClientRepository;

    @Autowired
    private LogService logService;

    @Autowired
    private JwtUtils jwtUtils;

    // General Exposed Endpoints ----------------------------
    // @Async("AsyncExecutor")
    // @GetMapping("/hello")
    // public String helloWorld() {
    // return "Hi!";
    // }

    // General Exposed Endpoints ----------------------------

    // API for INET ----------------------------------------------
    @Async("AsyncExecutor")
    @PostMapping("/executeProvision")
    // @PreAuthorize("hasAuthority('HIVECONNECT_PROVISIONING_ACTION')")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> executeInetProvision(@RequestBody Map<String, String> params,
            @RequestParam(required = false) String user, @RequestParam(required = false) String action,
            HttpServletRequest request)
            throws JsonMappingException, JsonProcessingException, InterruptedException {

        String networkType = "";
        System.out.println(">>> HiveService: Provision executed");

        // Prepare RequestBody Values
        String accountNo = params.get("accountNo");
        String clientName = params.get("clientName");
        String serialNumber = params.get("serialNumber");
        String macAddress = params.get("macAddress");
        // String cidr = params.get("cidr"); // Cidr block of site
        // String site = params.get("networkName"); // To determine IPAM site
        String oltIp = params.get("olt");
        Long oltId = Long.parseLong(params.get("oltId"));
        String site = oltRepo.findByOlt_ip(oltId).get().getOltNetworksite();
        // String wanMode = params.get("wanMode"); // Bridged or Routed

        String packageType = params.get("packageType");
        String upstream = params.get("upstream");
        String downstream = params.get("downstream");
        // String packageType = "PLAN999";
        // String upstream = "1000";
        // String downstream = "10000";

        // TODO: Dynamic Site, get actual IP Address according to Site

        // site = "CDO_3";
        String ipAddress = ipAddRepo
                .getOneAvailableIpAddressUnderSite(site, "Private")
                .get(0)
                .getIpAddress();

        String defaultGateway = ipAddRepo.getGatewayOfIpAddress(ipAddress.substring(0,
                (ipAddress.lastIndexOf("."))));

        // ACS Processes
        Optional<CidrIpAddress> ipAddressData = ipAddRepo.findByipAddress(ipAddress);
        String vlanId = ipAddressData.get().getVlanId();

        String acsPushResponse = executeInetAutoProv(accountNo, clientName, serialNumber, defaultGateway,
                ipAddress, vlanId);

        if (acsPushResponse.contains("Successful")) { // TODO: revert to monitoring for INET
            ResponseEntity responseEntity = executeInetMonitoring(accountNo, serialNumber, macAddress, clientName,
                    ipAddress, packageType, upstream,
                    downstream, oltIp);

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), accountNo,
                    String.valueOf(responseEntity.getStatusCode().value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

            return responseEntity;

        } else {
            // AcsController.deleteWanInstance(serialNumber);
            AcsController.rollbackSsid(serialNumber);

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), accountNo,
                    String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

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

        Optional<PackageTypeEntity> optionalPackage = packageRepo.findBypackageId(packageType);
        if (optionalPackage.isPresent()) {
            PackageTypeEntity packageT = optionalPackage.get();

            if (showBody)
                System.out.println(packageT.toString());

            packageName = packageT.getPackageType();
            upstream = packageT.getUpstream();
            downstream = packageT.getDownstream();

        }

        String ansibleApiUrl = playbookMonitoringApiUrl + "launch/";
        String accessToken = ansibleAccessToken;

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
                "\"job_template\": \"22\",\n" +
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

        ResponseEntity lastJobStatus = jobStatus(accountNo, jobId, false);

        if (lastJobStatus.getStatusCode().equals(HttpStatus.OK)) {
            // finalize and mark everything to be activated
            ipAddRepo.associateIpAddressToAccountNumber(accountNo, ipAddress);
            AcsController.setInformIntervalPostProv(serialNumber);
            AcsController.onuOnboarded(serialNumber);

            String ssidName = accountNo.replace(" ", "_");

            String oltInterface = getOltDetails(jobId);

            String[] bandwidth = getOltBandwidthRate(jobId);

            Optional<subscriberEntity> optionalClient = clientRepo.findBySubscriberAccountNumber(accountNo);
            if (optionalClient.isPresent()) {
                subscriberEntity client = optionalClient.get();
                client.setOnuDeviceName(deviceName);
                client.setOnuMacAddress(macAddress);
                client.setIpAssigned(ipAddress);
                client.setSubsStatus("ACTIVE");
                client.setBucketId("100");
                client.setOltReportedUpstream(upstream);
                client.setOltReportedDownstream(downstream);
                client.setOnuSerialNumber(serialNumber);
                client.setOltIp(oltIp);
                client.setPackageType(packageType);
                client.setSsidName(ssidName);
                // client.setSite(site);
                client.setProvision("HiveConnect");
                clientRepo.save(client);

                HiveClientService.addHiveNewClient(accountNo, client.getSubscriberName(), serialNumber, deviceName,
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
        jsonBody.append("\"accountNumber\":\"" + accountNumber + "\",");
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
    // @PreAuthorize("hasAuthority('HIVECONNECT_PROVISIONING_ACTION')")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> executeHiveAutoConfig(@RequestBody Map<String, String> params,
            @RequestParam(required = false) String user, @RequestParam(required = false) String action,
            HttpServletRequest request)
            throws JsonMappingException, JsonProcessingException, InterruptedException {

        String networkType = "";
        System.out.println(">>> HiveService: Provision executed from HiveApp");

        // Prepare RequestBody Values
        String accountNo = params.get("accountNo");
        String clientName = params.get("clientName");
        String serialNumber = params.get("serialNumber");
        String macAddress = params.get("macAddress");
        // String site = params.get("site"); // To determine IPAM site
        String oltIp = params.get("olt");
        Long oltId = Long.parseLong(params.get("oltId"));
        String site = oltRepo.findByOlt_ip(oltId).get().getOltNetworksite();
        // String site = oltRepo.findByOlt_ip(oltIp).get().getOltNetworksite();
        // String wanMode = params.get("wanMode"); // Bridged or Routed

        String packageType = params.get("packageType");
        // String upstream = params.get("upstream");
        // String downstream = params.get("downstream");
        String upstream = packageRepo.findBypackageId(packageType).get().getUpstream();
        String downstream = packageRepo.findBypackageId(packageType).get().getDownstream();
        // String packageType = "PLAN999";
        // String upstream = "1000";
        // String downstream = "10000";

        // site = "CDO_3";
        String ipAddress = ipAddRepo
                .getOneAvailableIpAddressUnderSite(site, "Private")
                .get(0)
                .getIpAddress();

        String defaultGateway = ipAddRepo.getGatewayOfIpAddress(ipAddress.substring(0,
                (ipAddress.lastIndexOf("."))));

        String packageName = "";

        Optional<PackageTypeEntity> optionalPackage = packageRepo.findBypackageId(packageType);
        if (optionalPackage.isPresent()) {
            PackageTypeEntity packageT = optionalPackage.get();
            if (showBody)
                System.out.println(packageT.toString());
            upstream = packageT.getUpstream();
            downstream = packageT.getDownstream();
            packageName = packageT.getPackageType();

        }

        String deviceName = "" + clientName.replace(" ", "_") + "_bw1";
        if (showBody)
            System.out.println(deviceName);

        // ACS Processes
        Optional<CidrIpAddress> ipAddressData = ipAddRepo.findByipAddress(ipAddress);
        String vlanId = ipAddressData.get().getVlanId();

        String acsResponse = executeInetAutoProv(accountNo, clientName, serialNumber, defaultGateway,
                ipAddress, vlanId);

        if (acsResponse.contains("Successful")) {
            // SET BANDWIDTH LIMIT HERE
            String ansibleApiUrl = playbookBandwidthLimitationApiUrl + "launch/";
            String accessToken = ansibleAccessToken;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            TimeUnit.SECONDS.sleep(20);
            AcsController.getWan2MacAddress(serialNumber);
            TimeUnit.SECONDS.sleep(20);

            String requestBody = "{\n" +
                    "\"job_template\": \"22\",\n" +
                    "\"ask_variables_on_launch\": \"true\",\n" +
                    "\"extra_vars\": \"---" +
                    "\\ndevice_name: " + deviceName +
                    "\\nserial_number: " + serialNumber +
                    "\\nmac_address: " + macAddress +
                    "\\nolt_ip: " + oltIp +
                    "\\naccount_number: " + accountNo + // TODO: add actual account number
                    "\\nstatus: Activated " +
                    "\\nonu_private_ip: " + ipAddress +
                    "\\ndownstream: " + downstream +
                    "\\nupstream: " + upstream +
                    "\\npackage: " + packageType + "\""
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

                    logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), accountNo,
                    String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

                return (ResponseEntity<Map<String, String>>) ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            ResponseEntity lastJobStatus = jobStatus(accountNo, jobId, true);

            if (lastJobStatus.getStatusCode().equals(HttpStatus.OK)) {
                // COMMENT FROM HERE
                // finalize and mark everything to be activated
                ipAddRepo.associateIpAddressToAccountNumber(accountNo, ipAddress);
                AcsController.setInformIntervalPostProv(serialNumber);
                AcsController.onuOnboarded(serialNumber);

                String ssidName = accountNo.replace(" ", "_");

                String oltInterface = getOltDetails(jobId);
                String[] bandwidth = getOltBandwidthRate(jobId);

                Optional<subscriberEntity> optionalClient = clientRepo.findBySubscriberAccountNumber(accountNo);
                if (optionalClient.isPresent()) {
                    subscriberEntity client = optionalClient.get();
                    client.setOnuDeviceName(deviceName);
                    client.setOnuMacAddress(macAddress);
                    client.setSubsStatus("ACTIVE");
                    client.setIpAssigned(ipAddress);
                    client.setBucketId("100");
                    client.setOltReportedUpstream(upstream);
                    client.setOltReportedDownstream(downstream);
                    client.setOnuSerialNumber(serialNumber);
                    client.setOltIp(oltIp);
                    client.setPackageType(packageType);
                    client.setSsidName(ssidName);
                    client.setSite(site);
                    client.setProvision("HiveConnect");
                    clientRepo.save(client);

                    HiveClientService.addHiveNewClient(accountNo, client.getSubscriberName(), serialNumber, deviceName,
                            macAddress, oltIp, oltInterface,
                            ipAddress,
                            ssidName, packageType, bandwidth[0], bandwidth[1]);

                    deviceRepo.updateParentBySerialNumber("Hive Test", serialNumber);
                }
                // END HERE

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), accountNo,
                    String.valueOf(lastJobStatus.getStatusCode().value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

                return lastJobStatus;

                // OLD CODE
                // Map<String, String> response = new HashMap<>();
                // response.put("status", "200");
                // response.put("message", acsResponse);
                // // can put change admin creds here
                // return ResponseEntity.status(HttpStatus.OK).body(response);

            } else {
                AcsController.deleteWanInstance(serialNumber);
                AcsController.rollbackSsid(serialNumber);

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), accountNo,
                    String.valueOf(lastJobStatus.getStatusCode().value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

                return lastJobStatus;
            }

        } else {
            AcsController.deleteWanInstance(serialNumber);
            AcsController.rollbackSsid(serialNumber);

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), accountNo,
                    String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

            Map<String, String> response = new HashMap<>();
            response.put("status", "500");
            response.put("message", acsResponse);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
        // return acsPushResponse;
    }

    // EXPOSE THIS API [USED FOR MIGRATION]
    @Async("AsyncExecutor")
    @PostMapping("/executeMigration")
    // @PreAuthorize("hasAuthority('HIVECONNECT_PROVISIONING_ACTION')")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> executeMigration(@RequestBody Map<String, String> params,
            @RequestParam(required = false) String user, @RequestParam(required = false) String action,
            HttpServletRequest request)
            throws JsonMappingException, JsonProcessingException, InterruptedException {

        System.out.println(">>> HiveService: Bucket to Migration executed from HiveApp");

        // Prepare RequestBody Values
        String accountNo = params.get("accountNo");
        if (accountNo == null) {

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), accountNo,
                    String.valueOf(HttpStatus.BAD_REQUEST.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

            Map<String, String> response = new HashMap<>();
            response.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
            response.put("message", "Subscriber accountNo is missing/empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        Optional<HiveClient> clientOptional = hiveClientRepository
                .findBySubscriberAccountNumber(accountNo);

        if (!clientOptional.isPresent()) { // if subscriber does not exist in database

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), accountNo,
                    String.valueOf(HttpStatus.NOT_FOUND.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

            Map<String, String> response = new HashMap<>();
            response.put("status", String.valueOf(HttpStatus.NOT_FOUND.value()));
            response.put("message", "Subscriber not found with account number: " + accountNo);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            HiveClient client = clientOptional.get();
            System.out.println(">>> HiveService: Subscriber found in database with status " + client.getStatus());
            if (!client.getStatus().contains("_PENDING_MIGRATION")) { // if subscriber is not pending for migration
                
                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), accountNo,
                    String.valueOf(HttpStatus.BAD_REQUEST.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

                Map<String, String> response = new HashMap<>();
                response.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
                response.put("message", "Cannot find any subscriber with account number: " + accountNo
                        + " that is pending for migration");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }

        String apiUrl = playbookMigrationUrl + "launch/";
        System.out.println(apiUrl);

        // Create headers with Content-Type set to application/json
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + ansibleMigrationToken);

        // Create a JSON request body
        StringBuilder jsonBody = new StringBuilder();

        jsonBody.append("{");
        jsonBody.append("\"job_template\":\"28\",");
        jsonBody.append("\"ask_variables_on_launch\":\"true\",");
        jsonBody.append("\"extra_vars\":\"---\\n" + "account_number: \\\"" + accountNo + "\\\"\""); // NOTE: gi add nalang nako syag double quotes sa account number mismo kay naay tendencies na if ang account no kay numbers lng (e.g. 12345), ang ma send pud dayon na request sa playbook kay gina treat as integer ang account no even though naka define na as string pagkuha sa params. i think ire-check nalng siguro ni soon
        jsonBody.append("}");

        System.out.println("Check accountNo if string: " + accountNo);

        String requestBody = jsonBody.toString();
        if (showBody)
            System.out.println(requestBody);

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(apiUrl,
                HttpMethod.POST, requestEntity,
                String.class);

        System.out.println(">>> HiveConnect: finished Bucket to Hive Migration");
        System.out.println("Response: " + response);

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

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), accountNo,
                String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), request.getRemoteAddr(),
                jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                request.getHeader("User-Agent"));

            return (ResponseEntity<Map<String, String>>) ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        ResponseEntity lastJobStatus = jobStatus(accountNo, jobId, false);

        if (lastJobStatus.getStatusCode().equals(HttpStatus.OK)) {

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), accountNo,
                    String.valueOf(lastJobStatus.getStatusCode().value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

            return lastJobStatus;
        } else {

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), accountNo,
                    String.valueOf(lastJobStatus.getStatusCode().value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

            return lastJobStatus;
        }
        // return acsPushResponse;
    }

    // APIs for HiveApp (end) ----------------------------------------------

    // AutoProvisioning

    @Async("AsyncExecutor")
    @PostMapping("/executeMonitoring")
    // @PreAuthorize("hasAuthority('HIVECONNECT_PROVISIONING_ACTION')")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> executeHiveMonitoring(@RequestBody Map<String, String> params,
            @RequestParam(required = false) String user, @RequestParam(required = false) String action,
            HttpServletRequest request)
            throws JsonMappingException, JsonProcessingException, InterruptedException {
        String accountNo = params.get("accountNo");
        String clientName = params.get("clientName");
        String serialNumber = params.get("serialNumber");
        String macAddress = params.get("macAddress");
        String oltIp = params.get("olt");
        Long oltId = Long.parseLong(params.get("oltId"));
        String site = oltRepo.findByOlt_ip(oltId).get().getOltNetworksite();
        // String site = oltRepo.findByOlt_ip(oltIp).get().getOltNetworksite();
        String ipAddress = ipAddRepo
                .getOneAvailableIpAddressUnderSite(site, "Private")
                .get(0)
                .getIpAddress();

        if (showBody)
            System.out.println(ipAddRepo
                    .getOneAvailableIpAddressUnderSite(site, "Private"));

        String packageType = params.get("packageType");
        // String upstream = params.get("upstream");
        // String downstream = params.get("downstream");
        String upstream = packageRepo.findBypackageId(packageType).get().getUpstream();
        String downstream = packageRepo.findBypackageId(packageType).get().getDownstream();
        // String packageType = "PLAN999";
        // String upstream = "1000";
        // String downstream = "10000";

        String packageName = "";

        Optional<PackageTypeEntity> optionalPackage = packageRepo.findBypackageId(packageType);
        if (optionalPackage.isPresent()) {
            PackageTypeEntity packageT = optionalPackage.get();
            if (showBody)
                System.out.println(packageT.toString());
            upstream = packageT.getUpstream();
            downstream = packageT.getDownstream();
            packageName = packageT.getPackageType();

        }

        String ansibleApiUrl = playbookMonitoringApiUrl + "launch/";
        String accessToken = ansibleAccessToken;

        String deviceName = "" + clientName.replace(" ", "_") + "_bw1";
        if (showBody)
            System.out.println(deviceName);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // TimeUnit.SECONDS.sleep(20);
        // AcsController.getWan2MacAddress(serialNumber);
        // TimeUnit.SECONDS.sleep(20);

        String requestBody = "{\n" +
                "\"job_template\": \"22\",\n" +
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

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), accountNo,
                String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), request.getRemoteAddr(),
                jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                request.getHeader("User-Agent"));

            return (ResponseEntity<Map<String, String>>) ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        ResponseEntity lastJobStatus = jobStatus(accountNo, jobId, false);

        if (lastJobStatus.getStatusCode().equals(HttpStatus.OK)) {
            // finalize and mark everything to be activated
            // ipAddRepo.associateIpAddressToAccountNumber(accountNo, ipAddress);
            // AcsController.setInformIntervalPostProv(serialNumber);
            // AcsController.onuOnboarded(serialNumber);

            // String ssidName = accountNo.replace(" ", "_");

            // String oltInterface = getOltDetails(jobId);
            // String[] bandwidth = getOltBandwidthRate(jobId);

            // Optional<subscriberEntity> optionalClient =
            // clientRepo.findBySubscriberAccountNumber(accountNo);
            // if (optionalClient.isPresent()) {
            // subscriberEntity client = optionalClient.get();
            // client.setOnuDeviceName(deviceName);
            // client.setOnuMacAddress(macAddress);
            // client.setSubsStatus("ACTIVE");
            // client.setIpAssigned(ipAddress);
            // client.setBucketId("100");
            // client.setOltReportedUpstream(upstream);
            // client.setOltReportedDownstream(downstream);
            // client.setOnuSerialNumber(serialNumber);
            // client.setOltIp(oltIp);
            // client.setPackageType(packageType);
            // client.setSsidName(ssidName);
            // client.setSite(site);
            // client.setProvision("HiveConnect");
            // clientRepo.save(client);

            // HiveClientService.addHiveNewClient(accountNo, client.getSubscriberName(),
            // serialNumber, deviceName,
            // macAddress, oltIp, oltInterface,
            // ipAddress,
            // ssidName, packageType, bandwidth[0], bandwidth[1]);

            // // Optional<ClientDetail> optionalClientDetail =
            // // clientDetailRepo.findByClientId(client.getId());
            // // if (optionalClientDetail.isPresent()) {
            // // ClientDetail clientDetail = optionalClientDetail.get();
            // // clientDetail.setStatus("finished");

            // // }

            // deviceRepo.updateParentBySerialNumber("Hive Test", serialNumber);
            // }

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), accountNo,
                    String.valueOf(lastJobStatus.getStatusCode().value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

            return lastJobStatus;
        } else {
            AcsController.deleteWanInstance(serialNumber);
            AcsController.rollbackSsid(serialNumber);

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), accountNo,
                    String.valueOf(lastJobStatus.getStatusCode().value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

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
    // @PreAuthorize("hasAuthority('HIVECONNECT_PROVISIONING_ACTION')")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> preprovisionCheck(@RequestBody Map<String, String> params,
            @RequestParam(required = false) String user, @RequestParam(required = false) String action,
            HttpServletRequest request)
            throws InterruptedException, JsonMappingException, JsonProcessingException {

        String jobId;

        System.out.println(">>> HiveService: Pre-Provision Check Initialized");

        String accountNo = params.get("accountNo");
        String clientName = params.get("clientName");
        String serialNumber = params.get("serialNumber");
        String macAddress = params.get("macAddress");
        String oltIp = params.get("olt");
        Long oltId = Long.parseLong(params.get("oltId"));
        String site = oltRepo.findByOlt_ip(oltId).get().getOltNetworksite();
        // String site = oltRepo.findByOlt_ip(oltIp).get().getOltNetworksite();
        String ipAddress = ipAddRepo
                .getOneAvailableIpAddressUnderSite(site, "Private")
                .get(0)
                .getIpAddress();

        String packageType = params.get("packageType");
        // String upstream = params.get("upstream");
        // String downstream = params.get("downstream");
        String upstream = packageRepo.findBypackageId(packageType).get().getUpstream();
        String downstream = packageRepo.findBypackageId(packageType).get().getDownstream();

        String ansibleApiUrl = playbookPreProvUrl + "launch/";
        String accessToken = ansibleAccessToken;

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

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), accountNo,
                    String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

                Map<String, String> response = new HashMap<>();
                response.put("status", "200");
                response.put("message", "All Clear. Proceed to Provisioning!");
                response.put("body", checkingResponse);
                return ResponseEntity.status(HttpStatus.OK).body(response);

            }

            else {

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), accountNo,
                String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), request.getRemoteAddr(),
                jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                request.getHeader("User-Agent"));

                Map<String, String> response = new HashMap<>();
                response.put("status", "500");
                response.put("message", errors.toString());
                response.put("body", checkingResponse);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        }

        logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), accountNo,
                    String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

        Map<String, String> response = new HashMap<>();
        response.put("status", "200");
        response.put("message", "No Result");
        response.put("body", checkingResponse);
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

    // Troubleshooting
    @Async("AsyncExecutor")
    @GetMapping("/lastJobStatus")
    public ResponseEntity<Map<String, String>> lastJobStatus(String accountNo, String jobId,
            boolean generateCredentials,
            @RequestParam(required = false) String user, @RequestParam(required = false) String action,
            HttpServletRequest request)
            throws JsonMappingException, JsonProcessingException, InterruptedException {

        // Monitor the job status
        String lastJobStatus = monitorJobStatus(jobId);

        // Handle job failure
        if (lastJobStatus.contains("fail")) {

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), jobId,
                    String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

            return handleJobFailure(jobId);
        }

        // Generate credentials if requested
        if (generateCredentials) {
            return generateCredentials(accountNo, jobId, user, action, request);
        }

        logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), jobId,
                    String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

        // Default response for successful job completion without credential generation
        Map<String, String> response = new HashMap<>();
        response.put("status", "200");
        response.put("message", "Job completed successfully.");
        response.put("awx_job_id", jobId);
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

    public ResponseEntity<Map<String, String>> jobStatus(String accountNo, String jobId,
            boolean generateCredentials)
            throws JsonMappingException, JsonProcessingException, InterruptedException {

        // Monitor the job status
        String lastJobStatus = monitorJobStatus(jobId);

        // Handle job failure
        if (lastJobStatus.contains("fail")) {
            return handleJobFailure(jobId);
        }

        // Generate credentials if requested
        if (generateCredentials) {
            return generateCreds(accountNo, jobId);
        }

        // Default response for successful job completion without credential generation
        Map<String, String> response = new HashMap<>();
        response.put("status", "200");
        response.put("message", "Job completed successfully.");
        response.put("awx_job_id", jobId);
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

    // Method to monitor job status
    private String monitorJobStatus(String jobId) throws InterruptedException {
        String ansibleApiUrl = playbookGetJobUrl + jobId;
        String accessToken = ansibleAccessToken;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = "";
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = null;
        String responseBody = null;

        StringBuilder tries = new StringBuilder();

        System.out.println("Trying to Get Job " + jobId);
        while (responseBody == null || responseBody.contains("\"finished\":null")) {
            TimeUnit.SECONDS.sleep(10);
            responseEntity = restTemplate.exchange(ansibleApiUrl, HttpMethod.GET, requestEntity, String.class);

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
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(responseBody);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            // Handle the exception appropriately, e.g., return a default status or throw a
            // custom exception
            return "error";
        }

        // Extract last job details
        String lastJobStatus = jsonNode.get("status").asText();

        // Print the results
        System.out.println("Job ID: " + jobId);
        System.out.println("Job Status: " + lastJobStatus);

        return lastJobStatus;
    }

    // Method to handle job failure
    private ResponseEntity<Map<String, String>> handleJobFailure(String jobId) {
        String ansibleApiUrl = "" + playbookGetJobUrl + jobId + "/job_events/?failed=True";
        String accessToken = ansibleAccessToken;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = "";
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(ansibleApiUrl, HttpMethod.GET, requestEntity,
                String.class);
        String stderr = responseEntity.getBody().toString();

        if (showBody)
            System.out.println(stderr);
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

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> response = new HashMap<>();
            response.put("status", "500");
            response.put("message", "An error occurred while processing the job status.");
            response.put("awx_job_id: ", jobId.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Method to generate credentials
    private ResponseEntity<Map<String, String>> generateCredentials(String accountNo, String jobId, @RequestParam(required = false) String user,
    @RequestParam(required = false) String action, HttpServletRequest request) {
        try {
            String ansibleApiUrl = "" + playbookGetJobUrl + jobId + "/stdout";
            String accessToken = ansibleAccessToken;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String requestBody = "";
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> responseEntity = restTemplate.exchange(ansibleApiUrl, HttpMethod.GET, requestEntity,
                    String.class);

            String responseBody = responseEntity.getBody();

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

            String newSsid = accountNo.replace(" ", "_");
            String password = "" + newSsid + "1234";

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), jobId,
                    String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

            Map<String, String> response = new HashMap<>();
            response.put("awx_job_id", jobId);
            response.put("status", "200");
            response.put("message", "Provisioning Successful!");
            response.put("ssid_name", newSsid + "2.4G/5G");
            response.put("ssid_pw", password);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {

            logService.logApiError(user, action, request.getMethod(), request.getRequestURI(), jobId,
                    String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), e.getMessage(), e.getStackTrace(),
                    request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

            Map<String, String> response = new HashMap<>();
            response.put("status", "500");
            response.put("message", "An error occurred while generating credentials.");
            response.put("awx_job_id", jobId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private ResponseEntity<Map<String, String>> generateCreds(String accountNo, String jobId) {
        try {
            String ansibleApiUrl = "" + playbookGetJobUrl + jobId + "/stdout";
            String accessToken = ansibleAccessToken;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String requestBody = "";
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> responseEntity = restTemplate.exchange(ansibleApiUrl, HttpMethod.GET, requestEntity,
                    String.class);

            String responseBody = responseEntity.getBody();

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

            String newSsid = accountNo.replace(" ", "_");
            String password = "" + newSsid + "1234";

            Map<String, String> response = new HashMap<>();
            response.put("awx_job_id", jobId);
            response.put("status", "200");
            response.put("message", "Provisioning Successful!");
            response.put("ssid_name", newSsid + "2.4G/5G");
            response.put("ssid_pw", password);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "500");
            response.put("message", "An error occurred while generating credentials.");
            response.put("awx_job_id", jobId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // @Async("AsyncExecutor")
    // @GetMapping("/lastJobStatus")
    // // @PreAuthorize("hasAuthority('HIVECONNECT_PROVISIONING_READ')")
    // @PreAuthorize("hasRole('USER')")
    // public ResponseEntity<Map<String, String>> lastJobStatus(String accountNo,
    // String jobId)
    // throws JsonMappingException, JsonProcessingException, InterruptedException {

    // String ansibleApiUrl = playbookGetJobUrl + jobId;
    // String accessToken = ansibleAccessToken;

    // HttpHeaders headers = new HttpHeaders();
    // headers.set("Authorization", "Bearer " + accessToken);
    // headers.setContentType(MediaType.APPLICATION_JSON);

    // String requestBody = "";
    // HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

    // RestTemplate restTemplate = new RestTemplate();
    // ResponseEntity<String> responseEntity = null;
    // String responseBody = null;

    // StringBuilder tries = new StringBuilder();

    // System.out.println("Trying Get Job " + jobId);
    // while (responseBody == null || responseBody.contains("\"finished\":null")) {
    // TimeUnit.SECONDS.sleep(10);
    // responseEntity = restTemplate.exchange(ansibleApiUrl, HttpMethod.GET,
    // requestEntity,
    // String.class);

    // if (responseEntity.getStatusCode() == HttpStatus.NOT_FOUND) {

    // tries.append("|");
    // System.out.println(tries.toString());
    // continue;
    // }
    // responseBody = responseEntity.getBody();
    // if (responseBody == null || responseBody.contains("\"finished\":null")) {
    // tries.append("|");
    // System.out.println(tries.toString());
    // continue;
    // }
    // }

    // if (showBody)
    // System.out.println(responseBody);
    // ObjectMapper objectMapper = new ObjectMapper();
    // JsonNode jsonNode = objectMapper.readTree(responseBody);

    // // Extract last job details
    // String lastJobStatus = jsonNode.get("status").asText();

    // // Print the results
    // System.out.println("Job ID: " + jobId);
    // System.out.println("Job Status: " + lastJobStatus);

    // if (lastJobStatus.contains("fail")) {

    // ansibleApiUrl = "" + playbookGetJobUrl + jobId + "/job_events/?failed=True";
    // requestEntity = new HttpEntity<>(requestBody, headers);

    // restTemplate = new RestTemplate();
    // responseEntity = restTemplate.exchange(ansibleApiUrl, HttpMethod.GET,
    // requestEntity,
    // String.class);
    // String stderr = responseEntity.getBody().toString();

    // if (showBody)
    // System.out.println(responseBody);
    // StringBuilder error = new StringBuilder();

    // try {

    // if (stderr.contains("Pseudo-terminal will not be allocated because stdin is
    // not a terminal"))
    // error.append("Bad OLT-IP.");

    // if (stderr.contains("name: OLT Vendor"))
    // error.append("Bad OLT-IP; OLT-IP not live.");

    // if (stderr.contains("Host with the same visible name"))
    // error.append("Client's device is already provisioned.");

    // if (stderr.contains("UnboundLocalError: local variable 'name' referenced
    // before assignment"))
    // error.append("Device on the OLT Interface already provisioned.");

    // if (stderr.contains("Duplicate termination found"))
    // error.append("IP Address already assigned to someone.");

    // if (stderr.contains("[prometheus]: UNREACHABLE! =>"))
    // error.append("Monitoring platform Prometheus is unreachable. Try again
    // later.");

    // if (stderr.contains("FAILED!") && stderr.contains("mac-address-table"))
    // error.append("Error on MAC Address Filtering.");

    // System.out.println("Errors: " + stderr);

    // Map<String, String> response = new HashMap<>();
    // response.put("status", "500");
    // response.put("message", error.toString());
    // response.put("awx_job_id: ", jobId.toString());
    // return
    // ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

    // }

    // catch (

    // Exception e) {
    // e.printStackTrace();
    // }
    // }

    // ansibleApiUrl = "" + playbookGetJobUrl + jobId + "/stdout";
    // requestEntity = new HttpEntity<>(requestBody, headers);

    // restTemplate = new RestTemplate();
    // responseEntity = restTemplate.exchange(ansibleApiUrl, HttpMethod.GET,
    // requestEntity,
    // String.class);

    // responseBody = responseEntity.getBody();

    // // Define the pattern
    // Pattern pattern =
    // Pattern.compile("\"olt_interface_bind\\.stdout\"\\s*:\\s*\"([^\"]+)\"");

    // // Create a matcher
    // Matcher matcher = pattern.matcher(responseBody);

    // // Find the match
    // if (matcher.find()) {
    // // Extract the desired value
    // String oltInterfaceBind = matcher.group(1);
    // System.out.println("olt_interface_bind.stdout: " + oltInterfaceBind);
    // } else {
    // System.out.println("Match not found");
    // }

    // String newSsid = accountNo.replace(" ", "_");
    // String password = "" + newSsid + "1234";

    // // return ("Job ID: " + jobId + "\nStatus: " + lastJobStatus + error);
    // Map<String, String> response = new HashMap<>();
    // response.put("awx_job_id", jobId);
    // response.put("status", "200");
    // response.put("message", "Provisioning and Monitoring Successful!");
    // response.put("ssid_name", newSsid + "2.4G/5G");
    // response.put("ssid_pw", password);
    // return ResponseEntity.status(HttpStatus.OK).body(response);
    // }

    // Get OLT Interface
    @Async("AsyncExecutor")
    @GetMapping("/getOltInterface")
    // @PreAuthorize("hasAuthority('HIVECONNECT_PROVISIONING_READ')")
    @PreAuthorize("hasRole('USER')")
    public String getOltInterface(String jobId,
            @RequestParam(required = false) String user, @RequestParam(required = false) String action,
            HttpServletRequest request) {

        String ansibleApiUrl = "" + playbookGetJobUrl + jobId + "/stdout";
        String accessToken = ansibleAccessToken;
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

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), jobId,
                    String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

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

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), jobId,
                    String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));
                    
            return vsolOltInterface;
        } else {
            System.out.println("OLT Interface Check: VSOL OLT Interface Match not found");
        }

        logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), jobId,
                    String.valueOf(HttpStatus.NOT_FOUND.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

        return "No OLT Interface found";
    }

    public String getOltDetails(String jobId) {

        String ansibleApiUrl = "" + playbookGetJobUrl + jobId + "/stdout";
        String accessToken = ansibleAccessToken;
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
    // @PreAuthorize("hasAuthority('HIVECONNECT_PROVISIONING_READ')")
    @PreAuthorize("hasRole('USER')")
    public String[] getOltBandwidth(String jobId,
            @RequestParam(required = false) String user, @RequestParam(required = false) String action,
            HttpServletRequest request) {

        String ansibleApiUrl = "" + playbookGetJobUrl + jobId + "/stdout";
        String accessToken = ansibleAccessToken;
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

        logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), jobId,
                    String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

        return bandwidth;
    }

    public String[] getOltBandwidthRate(String jobId) {

        String ansibleApiUrl = "" + playbookGetJobUrl + jobId + "/stdout";
        String accessToken = ansibleAccessToken;
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
    // @PreAuthorize("hasAuthority('HIVECONNECT_PROVISIONING_ACTION')")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> simulateError(String jobId,
            @RequestParam(required = false) String user, @RequestParam(required = false) String action,
            HttpServletRequest request) {

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), jobId,
                    String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

        Map<String, String> response = new HashMap<>();
        response.put("awx_job_id", jobId);
        response.put("status", "500");
        response.put("message", "Error on Mac Address Filtering!");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // ------7-24-24
    // @Async("asyncExecutor")
    // @PostMapping("/resetHiveDummy")
    // public String deleteClient() {
    // clientRepo.resetHiveDummy();
    // deviceRepo.resetHiveDummy();
    // return "Hive Demo Dummy Accounts cleared! Test Devices reverted to rogue!";
    // }
    // ---------

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
    // @PreAuthorize("hasAuthority('HIVECONNECT_PROVISIONING_READ')")
    @PreAuthorize("hasRole('USER')")
    public String testGetOltInterface(@PathVariable("jobId") String jobId,
            @RequestParam(required = false) String user, @RequestParam(required = false) String action,
            HttpServletRequest request) {

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), jobId,
                    String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

        return getOltDetails(jobId);
    }

    @Async("asyncExecutor")
    @GetMapping("/testExecuteMonitoring")
    // @PreAuthorize("hasAuthority('HIVECONNECT_PROVISIONING_READ')")
    @PreAuthorize("hasRole('USER')")
    public String testExecuteMonitoring(@RequestBody Map<String, String> params,
            @RequestParam(required = false) String user, @RequestParam(required = false) String action,
            HttpServletRequest request) {

        String accountNo = params.get("accountNo");
        String clientName = params.get("clientName");
        String serialNumber = params.get("serialNumber");
        String macAddress = params.get("macAddress");
        String oltIp = params.get("olt");
        Long oltId = Long.parseLong(params.get("oltId"));
        String site = oltRepo.findByOlt_ip(oltId).get().getOltNetworksite();
        // String site = oltRepo.findByOlt_ip(oltIp).get().getOltNetworksite();
        String ipAddress = ipAddRepo
                .getOneAvailableIpAddressUnderSite(site, "Private")
                .get(0)
                .getIpAddress();

        String packageType = params.get("packageType");
        String upstream = params.get("upstream");
        String downstream = params.get("downstream");
        // String packageType = "PLAN999";
        // String upstream = "1000";
        // String downstream = "10000";

        String deviceName = "" + clientName.replace(" ", "_") + "_bw1";

        String ssidName = accountNo.replace(" ", "_");

        String oltInterface = getOltDetails("1424");

        String[] bandwidth = getOltBandwidthRate("1424");

        Optional<subscriberEntity> optionalClient = clientRepo.findBySubscriberAccountNumber(accountNo);
        if (optionalClient.isPresent()) {
            subscriberEntity client = optionalClient.get();
            client.setOnuDeviceName(deviceName);
            client.setOnuMacAddress(macAddress);
            client.setSubsStatus("ACTIVE");
            client.setIpAssigned(ipAddress);
            client.setBucketId("100");
            client.setOltReportedUpstream(upstream);
            client.setOltReportedDownstream(downstream);
            client.setOnuSerialNumber(serialNumber);
            client.setOltIp(oltIp);
            client.setPackageType(packageType);
            client.setSsidName(ssidName);
            client.setSite(site);
            client.setProvision("HiveConnect");
            clientRepo.save(client);

            HiveClientService.addHiveNewClient(accountNo, client.getSubscriberName(), serialNumber, deviceName,
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
        
        logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), accountNo,
                    String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));
                    
        return "Check database";
    }

}