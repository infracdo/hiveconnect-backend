package com.autoprov.autoprov.controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.autoprov.autoprov.entity.acsDomain.Device;
import com.autoprov.autoprov.entity.hiveDomain.HiveClient;
import com.autoprov.autoprov.entity.subscriberDomain.subscriberEntity;
import com.autoprov.autoprov.repositories.acsRepositories.DeviceRepository;
import com.autoprov.autoprov.repositories.hiveRepositories.HiveClientRepository;
import com.autoprov.autoprov.repositories.subscriberRepositories.subscriberRepository;
import com.autoprov.autoprov.security.jwt.JwtUtils;
import com.autoprov.autoprov.services.LogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

@PropertySource("classpath:application.properties")
@CrossOrigin(origins = "*")
@RestController
public class AcsController {

    @Value("${playbookActivateClient}")
    private String playbookActivateClientApiUrl;

    @Value("${playbookDeactivateClient}")
    private String playbookDeactivateClientApiUrl;

    @Value("${ansibleAccessToken}")
    private String ansibleAccessToken;

    @Value("${playbookGetJobUrl}")
    private String playbookGetJobUrl;

    @Autowired
    private DeviceRepository DeviceRepo;

    @Autowired
    private HiveClientRepository hiveClientRepo;

    @Autowired
    private subscriberRepository subscriberRepo;

    @Autowired
    private LogService logService;

    @Autowired
    private JwtUtils jwtUtils;

    @Value("${acsApiUrl}")
    private static String acsApiUrl;

    // Exposed for HiveApp ----------------------------------------
    @Async("AsyncExecutor")
    @GetMapping("/getRogueDevices")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getRougeDevices(@RequestParam(required = false) String user,
            @RequestParam(required = false) String action, HttpServletRequest request) {
        try {
            List<Device> Device = new ArrayList<>();
            DeviceRepo.findByGroup("unassigned").forEach(Device::add);
            System.out.println("backend hive api accessed");

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), null,
                    String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

            return new ResponseEntity<>(Device, HttpStatus.OK);
        } catch (Exception e) {

            logService.logApiError(user, action, request.getMethod(), request.getRequestURI(), null,
                    String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), e.getMessage(), e.getStackTrace(),
                    request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Exposed for HiveApp (end) ----------------------------------------

    // [[[[[[---------------Exposed APIs for Connect-Disconnect [REQUIRES AUTH AND
    // TESTING]

    // --------------deactivateSubscriber base on accountNumber---------------------
    // [USED FOR BILLING]
    @Async("AsyncExecutor")
    @PostMapping("/deactivateSubscriber")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> disconnectClient(@RequestBody Map<String, String> params,
            @RequestParam(required = false) String user, @RequestParam(required = false) String action,
            HttpServletRequest request) throws JsonMappingException, JsonProcessingException, InterruptedException {

        Map<String, String> response = new LinkedHashMap<>(); // Use String as the value type
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String subscriberAccountNumber = params.get("subscriberAccountNumber");

        // Check if the subscriber account number is empty or null
        if (subscriberAccountNumber == null || subscriberAccountNumber.isEmpty()) {

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), subscriberAccountNumber,
                    String.valueOf(HttpStatus.BAD_REQUEST.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

            response.put("timestamp", timestamp);
            response.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
            response.put("message", "Subscriber account number is empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Fetch the client from the repository based on the account number
        Optional<HiveClient> optionalClient = hiveClientRepo
                .findBySubscriberAccountNumber(subscriberAccountNumber);
        if (!optionalClient.isPresent()) {
            
            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), subscriberAccountNumber,
                    String.valueOf(HttpStatus.CONFLICT.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

            response.put("timestamp", timestamp);
            response.put("status", String.valueOf(HttpStatus.CONFLICT.value()));
            response.put("message", "Subscriber account number does not exist: " + subscriberAccountNumber);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        HiveClient subscriber = optionalClient.get();

        // Check if the subscriber is active
        // if (client.getSubsStatus() == null ||
        // !client.getSubsStatus().equals("ACTIVE")||
        // !client.getSubsStatus().equals("Activated")) {
        // response.put("timestamp", timestamp);
        // response.put("status", String.valueOf(HttpStatus.CONFLICT.value()));
        // response.put("message", "subscriber not active");
        // return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        // }
        if (subscriber.getStatus() == null ||
                (!subscriber.getStatus().equalsIgnoreCase("ACTIVE") && !subscriber.getStatus().equalsIgnoreCase("Activated"))) {
            
                    logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), subscriberAccountNumber,
                    String.valueOf(HttpStatus.CONFLICT.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

            response.put("timestamp", timestamp);
            response.put("status", String.valueOf(HttpStatus.CONFLICT.value()));
            response.put("message", "Subscriber is not active");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        // Get the serial number from the client
        // String serialNumber = subscriber.getOnuSerialNumber();
        // if (serialNumber == null) {

        //     logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), subscriberAccountNumber,
        //             String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), request.getRemoteAddr(),
        //             jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
        //             request.getHeader("User-Agent"));

        //     response.put("timestamp", timestamp);
        //     response.put("status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
        //     response.put("message", "Serial number is not available for account number: " + subscriberAccountNumber);
        //     return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        // }

        // Call to ACS to Disconnect Wan2
        // String apiUrl = acsApiUrl + "toggleWan";

        // Execute playbook to deactivate client
        String apiUrl = playbookDeactivateClientApiUrl + "launch/";
        System.out.println("Deactivate Client API URL: " + apiUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + ansibleAccessToken);

        // String instance = "2";
        // String toggle = "0";

        // Create a JSON request body
        StringBuilder jsonBody = new StringBuilder();
        // jsonBody.append("{");
        // jsonBody.append("\"serialNumber\":\"").append(serialNumber).append("\",");
        // jsonBody.append("\"Instance\":\"").append(instance).append("\",");
        // jsonBody.append("\"Toggle\":\"").append(toggle).append("\"");
        // jsonBody.append("}");

        jsonBody.append("{");
        jsonBody.append("\"job_template\":\"24\",");
        jsonBody.append("\"ask_variables_on_launch\":\"true\",");
        jsonBody.append("\"extra_vars\":\"---\\n" + "account_number: \\\"" + subscriberAccountNumber + "\\\"\""); // NOTE: gi add nalang nako syag double quotes sa account number mismo kay naay tendencies na if ang account no kay numbers lng (e.g. 12345), ang ma send pud dayon na request sa playbook kay gina treat as integer ang account no even though naka define na as string pagkuha sa params. i think ire-check nalng siguro ni soon
        jsonBody.append("}");
        
        String jsonRequestBody = jsonBody.toString();
        System.out.println(jsonRequestBody);

        HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        // String jsonResponse = restTemplate.postForObject(apiUrl, requestEntity, String.class);
        ResponseEntity<String> playbookResponse = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, String.class);
        
        // System.out.println("HiveConnect Pushed: subscriber successfully deactivated");
        System.out.println("Response: " + playbookResponse);

        String jobId;
        if (playbookResponse.getStatusCode() == HttpStatus.CREATED) {
            System.out.println("Request Successful.");
            String responseBody = playbookResponse.getBody();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            jobId = jsonNode.get("id").asText();

        } else {
            System.out.println("Request failed. Response: " + playbookResponse.getStatusCode());
            return (ResponseEntity<Map<String, String>>) ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        ResponseEntity lastJobStatus = jobStatus(subscriberAccountNumber, jobId, false);

        if (lastJobStatus.getStatusCode().equals(HttpStatus.OK)) {
            subscriber.setStatus("ONHOLD");
            hiveClientRepo.save(subscriber);
            return lastJobStatus;
        } else {
            return lastJobStatus;
        }

        // Handle the response and update the client status
        // if (jsonResponse.contains("Pushed")) {
        //     subscriber.setSubsStatus("DEACTIVATED");
        //     subscriberRepo.save(subscriber);

        //     logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), subscriberAccountNumber,
        //             String.valueOf(HttpStatus.CREATED.value()), request.getRemoteAddr(),
        //             jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
        //             request.getHeader("User-Agent"));

        //     response.put("timestamp", timestamp);
        //     response.put("status", String.valueOf(HttpStatus.CREATED.value()));
        //     response.put("message", "Subscriber successfully deactivated");
        //     return ResponseEntity.status(HttpStatus.CREATED).body(response);
        // } else {

        //     logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), subscriberAccountNumber,
        //             String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), request.getRemoteAddr(),
        //             jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
        //             request.getHeader("User-Agent"));

        //     response.put("timestamp", timestamp);
        //     response.put("status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
        //     response.put("message", jsonResponse);
        //     return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        // }
    }

    // -------POST END POINT BASE ON ACCOUNT NUMBER SEND FOR TEMPORARY DISCONNECTION
    // @Async("AsyncExecutor")
    // @PostMapping("/deactivateSubscriber")
    // public ResponseEntity<Map<String, String>> disconnectClient(@RequestBody
    // Map<String, String> params) {
    // // Extract account number from request body
    // String accountNumber = params.get("accountNumber");

    // // Fetch client from repository
    // Optional<HiveClient> optionalClient =
    // hiveClientRepository.findBySubscriberAccountNumber(accountNumber);
    // if (!optionalClient.isPresent()) {
    // return ResponseEntity.status(HttpStatus.NOT_FOUND)
    // .body(Collections.singletonMap("message", "Client not found for account
    // number: " + accountNumber));
    // }

    // HiveClient client = optionalClient.get();

    // // Get the serial number from the client
    // String serialNumber = client.getOnuSerialNumber();
    // if (serialNumber == null) {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body(Collections.singletonMap("message", "Serial number is not available for
    // account number: " + accountNumber));
    // }

    // // Call to ACS to Disconnect Wan2
    // String apiUrl = acsApiUrl + "toggleWan";

    // // Create headers with Content-Type set to application/json
    // HttpHeaders headers = new HttpHeaders();
    // headers.setContentType(MediaType.APPLICATION_JSON);

    // String instance = "2";
    // String toggle = "0";

    // // Create a JSON request body
    // StringBuilder jsonBody = new StringBuilder();
    // jsonBody.append("{");
    // jsonBody.append("\"serialNumber\":\"").append(serialNumber).append("\",");
    // jsonBody.append("\"Instance\":\"").append(instance).append("\",");
    // jsonBody.append("\"Toggle\":\"").append(toggle).append("\"");
    // jsonBody.append("}");

    // String jsonRequestBody = jsonBody.toString();
    // System.out.println(jsonRequestBody);

    // HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody,
    // headers);
    // RestTemplate restTemplate = new RestTemplate();
    // String jsonResponse = restTemplate.postForObject(apiUrl, requestEntity,
    // String.class);

    // System.out.println("HiveConnect: ACS Push: WAN2 Disable Task Pushed");
    // System.out.println("Response: " + jsonResponse);

    // // Handle response and update client status
    // if (jsonResponse.contains("Pushed")) {
    // // Update client status to 'deactivated'
    // client.setStatus("deactivated");
    // hiveClientRepository.save(client);

    // Map<String, String> response = new HashMap<>();
    // response.put("status", "200");
    // response.put("message", "HiveConnect: ACS Push: WAN2 Disable Task Pushed");
    // return ResponseEntity.status(HttpStatus.OK).body(response);
    // } else {
    // Map<String, String> response = new HashMap<>();
    // response.put("status", "500");
    // response.put("message", jsonResponse);
    // return
    // ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    // }
    // }

    // ----------------Activate/Reconnect Subscriber [USED FOR BILLING]
    @Async("AsyncExecutor")
    @PostMapping("/activateSubscriber")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> reconnectClient(@RequestBody Map<String, String> params,
            @RequestParam(required = false) String user, @RequestParam(required = false) String action,
            HttpServletRequest request) throws JsonMappingException, JsonProcessingException, InterruptedException {

        System.out.println(">>> HiveService: Activate Subscriber executed from ABS");

        Map<String, String> response = new LinkedHashMap<>();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String subscriberAccountNumber = params.get("subscriberAccountNumber");

        // Check if the subscriber account number is empty or null
        if (subscriberAccountNumber == null || subscriberAccountNumber.isEmpty()) {

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), subscriberAccountNumber,
                    String.valueOf(HttpStatus.BAD_REQUEST.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

            response.put("timestamp", timestamp);
            response.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
            response.put("message", "Subscriber account number is empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Fetch client from repository
        Optional<HiveClient> clientOptional = hiveClientRepo
                .findBySubscriberAccountNumber(subscriberAccountNumber);
        if (!clientOptional.isPresent()) {

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), subscriberAccountNumber,
                    String.valueOf(HttpStatus.NOT_FOUND.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

            response.put("timestamp", timestamp);
            response.put("status", String.valueOf(HttpStatus.NOT_FOUND.value()));
            response.put("message", "Subscriber does not exist for account number: " + subscriberAccountNumber);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        HiveClient client = clientOptional.get();

        // Check if the subscriber is deactivated
        if (client.getStatus() == null || !client.getStatus().equalsIgnoreCase("ONHOLD")) {

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), subscriberAccountNumber,
                    String.valueOf(HttpStatus.CONFLICT.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

            response.put("timestamp", timestamp);
            response.put("status", String.valueOf(HttpStatus.CONFLICT.value()));
            response.put("message", "Subscriber not on hold");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        // Get the serial number from the client
        // String serialNumber = client.getOnuSerialNumber();
        // if (serialNumber == null) {

        //     logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), subscriberAccountNumber,
        //             String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), request.getRemoteAddr(),
        //             jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
        //             request.getHeader("User-Agent"));

        //     response.put("timestamp", timestamp);
        //     response.put("status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
        //     response.put("message", "Serial number is not available for account number: " + subscriberAccountNumber);
        //     return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        // }

        // Call to ACS to Reconnect Wan2
        // String apiUrl = acsApiUrl + "toggleWan";
        
        // Execute playbook to activate client
        String apiUrl = playbookActivateClientApiUrl + "launch/";
        System.out.println("Activate Client Playbook API URL: " + apiUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + ansibleAccessToken);

        // String instance = "2";
        // String toggle = "1"; // Toggle value for reconnect

        // Create a JSON request body
        StringBuilder jsonBody = new StringBuilder();
        // jsonBody.append("{");
        // jsonBody.append("\"serialNumber\":\"").append(serialNumber).append("\",");
        // jsonBody.append("\"Instance\":\"").append(instance).append("\",");
        // jsonBody.append("\"Toggle\":\"").append(toggle).append("\"");
        // jsonBody.append("}");

        jsonBody.append("{");
        jsonBody.append("\"job_template\":\"23\",");
        jsonBody.append("\"ask_variables_on_launch\":\"true\",");
        jsonBody.append("\"extra_vars\":\"---\\n" + "account_number: \\\"" + subscriberAccountNumber + "\\\"\""); // NOTE: gi add nalang nako syag double quotes sa account number mismo kay naay tendencies na if ang account no kay numbers lng (e.g. 12345), ang ma send pud dayon na request sa playbook kay gina treat as integer ang account no even though naka define na as string pagkuha sa params. i think ire-check nalng siguro ni soon
        jsonBody.append("}");

        String jsonRequestBody = jsonBody.toString();
        System.out.println(jsonRequestBody);

        HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        // String jsonResponse = restTemplate.postForObject(apiUrl, requestEntity, String.class);
        ResponseEntity<String> playbookResponse = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, String.class);

        // System.out.println("HiveConnect Pushed:subscriber successfully activated");
        System.out.println("Response: " + playbookResponse);

        String jobId;
        if (playbookResponse.getStatusCode() == HttpStatus.CREATED) {
            System.out.println("Request Successful.");
            String responseBody = playbookResponse.getBody();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            jobId = jsonNode.get("id").asText();

        } else {
            System.out.println("Request failed. Response: " + playbookResponse.getStatusCode());
            return (ResponseEntity<Map<String, String>>) ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        ResponseEntity lastJobStatus = jobStatus(subscriberAccountNumber, jobId, false);

        if (lastJobStatus.getStatusCode().equals(HttpStatus.OK)) {
            client.setStatus("ACTIVE");
            hiveClientRepo.save(client);
            return lastJobStatus;
        } else {
            return lastJobStatus;
        }

        // Handle response
        // if (jsonResponse.contains("Pushed")) {
        //     // Update client status to 'active'
        //     client.setSubsStatus("ACTIVE");
        //     subscriberRepo.save(client);

        //     logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), subscriberAccountNumber,
        //             String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
        //             jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
        //             request.getHeader("User-Agent"));

        //     response.put("timestamp", timestamp);
        //     response.put("status", String.valueOf(HttpStatus.OK.value()));
        //     response.put("message", "HiveConnect: Subscriber successfully activated");
        //     return ResponseEntity.status(HttpStatus.OK).body(response);
        // } else {

        //     logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), subscriberAccountNumber,
        //             String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), request.getRemoteAddr(),
        //             jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
        //             request.getHeader("User-Agent"));
                    
        //     response.put("timestamp", timestamp);
        //     response.put("status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
        //     response.put("message", jsonResponse);
        //     return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        // }
    }

    // Method to check the job status
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
        response.put("status", String.valueOf(HttpStatus.OK.value()));
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

        // if (showBody)
        //     System.out.println(responseBody);

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

        // if (showBody)
        //     System.out.println(stderr);
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
            response.put("status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            response.put("message", error.toString());
            response.put("awx_job_id: ", jobId.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> response = new HashMap<>();
            response.put("status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            response.put("message", "An error occurred while processing the job status.");
            response.put("awx_job_id: ", jobId.toString());
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
            response.put("status", String.valueOf(HttpStatus.OK.value()));
            response.put("message", "Provisioning Successful!");
            response.put("ssid_name", newSsid + "2.4G/5G");
            response.put("ssid_pw", password);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            response.put("message", "An error occurred while generating credentials.");
            response.put("awx_job_id", jobId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // POST end point to Recconect Internet based on account NUmber sent from
    // billing
    // @Async("AsyncExecutor")
    // @PostMapping("/activateSubscriber")
    // public ResponseEntity<Map<String, String>> reconnectClient(@RequestBody
    // Map<String, String> params) {
    // // Extract account number from request body
    // String subscriberAccountNumber = params.get("subscriberAccountNumber");

    // // Fetch client from repository
    // Optional<HiveClient> clientOptional =
    // hiveClientRepository.findBySubscriberAccountNumber(subscriberAccountNumber);
    // if (!clientOptional.isPresent()) {
    // return ResponseEntity.status(HttpStatus.NOT_FOUND)
    // .body(Collections.singletonMap("message", "Client not found for account
    // number: " + subscriberAccountNumber));
    // }

    // HiveClient client = clientOptional.get();

    // // Get the serial number from the client
    // String serialNumber = client.getOnuSerialNumber();

    // // Call to ACS to Reconnect Wan2
    // String apiUrl = acsApiUrl + "toggleWan";

    // // Create headers with Content-Type set to application/json
    // HttpHeaders headers = new HttpHeaders();
    // headers.setContentType(MediaType.APPLICATION_JSON);

    // String instance = "2";
    // String toggle = "1"; // Toggle value for reconnect

    // // Create a JSON request body
    // StringBuilder jsonBody = new StringBuilder();
    // jsonBody.append("{");
    // jsonBody.append("\"serialNumber\":\"").append(serialNumber).append("\",");
    // jsonBody.append("\"Instance\":\"").append(instance).append("\",");
    // jsonBody.append("\"Toggle\":\"").append(toggle).append("\"");
    // jsonBody.append("}");

    // String jsonRequestBody = jsonBody.toString();
    // System.out.println(jsonRequestBody);

    // HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody,
    // headers);
    // RestTemplate restTemplate = new RestTemplate();
    // String jsonResponse = restTemplate.postForObject(apiUrl, requestEntity,
    // String.class);

    // System.out.println("HiveConnect: ACS Push: WAN2 Enable Task Pushed");
    // System.out.println("Response: " + jsonResponse);

    // // Handle response
    // if (jsonResponse.contains("Pushed")) {
    // // Update client status to 'active'
    // client.setStatus("active");
    // hiveClientRepository.save(client);

    // Map<String, String> response = new HashMap<>();
    // response.put("status", "200");
    // response.put("message", "HiveConnect: ACS Push: WAN2 Enable Task Pushed");
    // return ResponseEntity.status(HttpStatus.OK).body(response);
    // } else {
    // Map<String, String> response = new HashMap<>();
    // response.put("status", "500");
    // response.put("message", jsonResponse);
    // return
    // ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    // }
    // }

    // ----------------UPDATE PACKAGE----no function yet [USED FOR BILLING]
    @Async("AsyncExecutor")
    @PostMapping("/updateSubscriberPackage")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> updateSubscriberPackage(@RequestBody Map<String, String> params,
            @RequestParam(required = false) String user, @RequestParam(required = false) String action,
            HttpServletRequest request) {
        Map<String, String> response = new LinkedHashMap<>();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String subscriberAccountNumber = params.get("subscriberAccountNumber");
        String packageType = params.get("packageType");

        // Check if the subscriber account number is empty or null
        if (subscriberAccountNumber == null || subscriberAccountNumber.isEmpty()) {

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), subscriberAccountNumber,
                    String.valueOf(HttpStatus.BAD_REQUEST.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

            response.put("timestamp", timestamp);
            response.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
            response.put("message", "Subscriber account number is empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Check if the package type is empty or null
        if (packageType == null || packageType.isEmpty()) {

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), subscriberAccountNumber,
                    String.valueOf(HttpStatus.BAD_REQUEST.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

            response.put("timestamp", timestamp);
            response.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
            response.put("message", "Package type is empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // // Fetch package from repository
        // Optional<subscriberEntity> packageOptional =
        // subscriberRepo.findByPackageType(packageType);
        // if (!packageOptional.isPresent()) {
        // response.put("timestamp", timestamp);
        // response.put("status", String.valueOf(HttpStatus.NOT_FOUND.value()));
        // response.put("message", "Package does not exist for type: " + packageType);
        // return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        // }

        // Fetch client from repository
        Optional<subscriberEntity> clientOptional = subscriberRepo
                .findBySubscriberAccountNumber(subscriberAccountNumber);
        if (!clientOptional.isPresent()) {

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), subscriberAccountNumber,
                    String.valueOf(HttpStatus.NOT_FOUND.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

            response.put("timestamp", timestamp);
            response.put("status", String.valueOf(HttpStatus.NOT_FOUND.value()));
            response.put("message", "Subscriber account number does not exist ");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        try {
            // Get the client entity
            subscriberEntity client = clientOptional.get();

            // Update the client entity with new package type
            client.setPackageType(packageType);

            // Optionally, update other relevant fields if necessary
            // Example: client.setUpdatedAt(LocalDateTime.now());

            // Save the updated client entity
            subscriberRepo.save(client);

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), subscriberAccountNumber,
                    String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

            // Prepare success response
            response.put("timestamp", timestamp);
            response.put("status", String.valueOf(HttpStatus.OK.value()));
            response.put("message", "Subscriber package successfully updated");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {

            logService.logApiError(user, action, request.getMethod(), request.getRequestURI(), subscriberAccountNumber,
                    String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), e.getMessage(), e.getStackTrace(),
                    request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

            // Handle any unexpected exceptions
            response.put("timestamp", timestamp);
            response.put("status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            response.put("message", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ----------------UPDATE PROVISION STATUS---no function yet [USED FOR BILLING]
    @Async("AsyncExecutor")
    @PostMapping("/updateSubscriberProvision")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> updateSubscriberProvision(@RequestBody Map<String, String> params,
            @RequestParam(required = false) String user, @RequestParam(required = false) String action,
            HttpServletRequest request) {
        Map<String, String> response = new LinkedHashMap<>();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String subscriberAccountNumber = params.get("subscriberAccountNumber");
        String provision = params.get("provision");

        // Check if the subscriber account number is empty or null
        if (subscriberAccountNumber == null || subscriberAccountNumber.isEmpty()) {

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), subscriberAccountNumber,
                    String.valueOf(HttpStatus.BAD_REQUEST.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

            response.put("timestamp", timestamp);
            response.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
            response.put("message", "Subscriber account number is empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Check if the provision is empty or null
        if (provision == null || provision.isEmpty()) {

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), subscriberAccountNumber,
                    String.valueOf(HttpStatus.BAD_REQUEST.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

            response.put("timestamp", timestamp);
            response.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
            response.put("message", "Provision is empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Validate provision type
        String provisionUpperCase = provision.toUpperCase();
        if (!(provisionUpperCase.equals("HIVECONNECT") || provisionUpperCase.equals("HIVE")
                || provisionUpperCase.equals("BUCKET"))) {

                    logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), subscriberAccountNumber,
                    String.valueOf(HttpStatus.BAD_REQUEST.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

            response.put("timestamp", timestamp);
            response.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
            response.put("message", "Provision type does not exist");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Fetch subscriber from repository
        Optional<subscriberEntity> clientOptional = subscriberRepo
                .findBySubscriberAccountNumber(subscriberAccountNumber);
        if (!clientOptional.isPresent()) {

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), subscriberAccountNumber,
                    String.valueOf(HttpStatus.NOT_FOUND.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

            response.put("timestamp", timestamp);
            response.put("status", String.valueOf(HttpStatus.NOT_FOUND.value()));
            response.put("message", "Subscriber does not exist for account number: " + subscriberAccountNumber);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        try {
            // Get the client entity
            subscriberEntity client = clientOptional.get();
            String currentProvisionUpperCase = client.getProvision().toUpperCase();
            String subsStatus = client.getSubsStatus();
            System.out.println("Current Status: " + subsStatus);

            // Check if provision contains specific words, ignoring case
            if (provisionUpperCase.contains("HIVECONNECT") || provisionUpperCase.contains("HIVE")) {
                if ("NEW".equalsIgnoreCase(subsStatus)) {

                    logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), subscriberAccountNumber,
                    String.valueOf(HttpStatus.BAD_REQUEST.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

                    response.put("timestamp", timestamp);
                    response.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
                    response.put("message", "This account number is not yet provisioned");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                } else {
                    if (currentProvisionUpperCase.contains("HIVECONNECT")
                            || currentProvisionUpperCase.contains("HIVE")) {

                                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), subscriberAccountNumber,
                    String.valueOf(HttpStatus.BAD_REQUEST.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

                        response.put("timestamp", timestamp);
                        response.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
                        response.put("message", "Subscriber is already provisioned to HiveConnect");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                    } else {
                        client.setProvision("HiveConnect");
                    }
                }
            } else if ("BUCKET".equalsIgnoreCase(provision)) {
                if ("NEW".equalsIgnoreCase(subsStatus)) {

                    logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), subscriberAccountNumber,
                    String.valueOf(HttpStatus.BAD_REQUEST.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

                    response.put("timestamp", timestamp);
                    response.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
                    response.put("message", "This account number is not yet provisioned");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                } else {
                    if ("BUCKET".equalsIgnoreCase(currentProvisionUpperCase)) {

                        logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), subscriberAccountNumber,
                    String.valueOf(HttpStatus.BAD_REQUEST.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

                        response.put("timestamp", timestamp);
                        response.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
                        response.put("message", "Subscriber is already provisioned to Bucket");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                    } else {
                        client.setProvision("Bucket");
                    }
                }
            }

            // Save the updated client entity
            subscriberRepo.save(client);

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), subscriberAccountNumber,
                    String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

            // Prepare success response
            response.put("timestamp", timestamp);
            response.put("status", String.valueOf(HttpStatus.OK.value()));
            response.put("message", "Subscriber provision successfully updated to " + client.getProvision());
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (Exception e) {

            logService.logApiError(user, action, request.getMethod(), request.getRequestURI(), subscriberAccountNumber,
                    String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), e.getMessage(), e.getStackTrace(),
                    request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));
                    
            // Handle any unexpected exceptions
            response.put("timestamp", timestamp);
            response.put("status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            response.put("message", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // permanently disconnect [USED FOR BILLING]
    @Async("AsyncExecutor")
    @PostMapping("/terminateSubscriber")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> permanentDisconnectClient(@RequestBody Map<String, String> params,
            @RequestParam(required = false) String user, @RequestParam(required = false) String action,
            HttpServletRequest request) {
        Map<String, String> response = new LinkedHashMap<>();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String subscriberAccountNumber = params.get("subscriberAccountNumber");

        if (subscriberAccountNumber == null || subscriberAccountNumber.isEmpty()) {

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), subscriberAccountNumber,
                    String.valueOf(HttpStatus.BAD_REQUEST.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

            response.put("timestamp", timestamp);
            response.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
            response.put("message", "Subscriber account number is empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        Optional<HiveClient> clientOptional = hiveClientRepo
                .findBySubscriberAccountNumber(subscriberAccountNumber);
        if (!clientOptional.isPresent()) {

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), subscriberAccountNumber,
                    String.valueOf(HttpStatus.NOT_FOUND.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

            response.put("timestamp", timestamp);
            response.put("status", String.valueOf(HttpStatus.NOT_FOUND.value()));
            response.put("message", "Subscriber does not exist for account number: " + subscriberAccountNumber);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        HiveClient client = clientOptional.get();

        if (client.getStatus() == null || !client.getStatus().equalsIgnoreCase("ONHOLD")) {

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), subscriberAccountNumber,
                    String.valueOf(HttpStatus.CONFLICT.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

            response.put("timestamp", timestamp);
            response.put("status", String.valueOf(HttpStatus.CONFLICT.value()));
            response.put("message", "Subscriber not inactive (need to deactivate account first)");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        String serialNumber = client.getOnuSerialNumber();
        if (serialNumber == null) {

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), subscriberAccountNumber,
                    String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

            response.put("timestamp", timestamp);
            response.put("status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            response.put("message", "Serial number is not available for account number: " + subscriberAccountNumber);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        try {
            String apiUrl = acsApiUrl + "deleteWanInstance";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String instance = "2";
            String jsonBody = String.format("{\"serialNumber\":\"%s\",\"Instance\":\"%s\"}", serialNumber, instance);

            HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);
            RestTemplate restTemplate = new RestTemplate(); // Consider using a RestTemplate bean
            String jsonResponse = restTemplate.postForObject(apiUrl, requestEntity, String.class);

            if (jsonResponse != null && jsonResponse.toLowerCase().contains("successful")) {
                // client.setStatus("TERMINATED");
                // hiveClientRepo.save(client);
                hiveClientRepo.delete(client);

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), subscriberAccountNumber,
                        String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                        jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                        request.getHeader("User-Agent"));

                response.put("timestamp", timestamp);
                response.put("status", String.valueOf(HttpStatus.OK.value()));
                response.put("message", "HiveConnect: account terminated successfully");
                return ResponseEntity.status(HttpStatus.OK).body(response);
            } else {

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), subscriberAccountNumber,
                    String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

                response.put("timestamp", timestamp);
                response.put("status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
                response.put("message", jsonResponse != null ? jsonResponse : "Unknown error");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {

            logService.logApiError(user, action, request.getMethod(), request.getRequestURI(), subscriberAccountNumber,
                    String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), e.getMessage(), e.getStackTrace(),
                    request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

            response.put("timestamp", timestamp);
            response.put("status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            response.put("message", "Exception occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // @Async("AsyncExecutor")
    // @PostMapping("/terminateSubscriber")
    // public ResponseEntity<Map<String, String>>
    // permanentDisconnectClient(@RequestBody Map<String, String> params) {
    // Map<String, String> response = new LinkedHashMap<>();
    // String timestamp =
    // LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd
    // HH:mm:ss"));
    // String subscriberAccountNumber = params.get("subscriberAccountNumber");

    // // Check if the subscriber account number is empty or null
    // if (subscriberAccountNumber == null || subscriberAccountNumber.isEmpty()) {
    // response.put("timestamp", timestamp);
    // response.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
    // response.put("message", "subscriber account number is empty");
    // return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    // }

    // // Fetch client from repository
    // Optional<subscriberEntity> clientOptional =
    // subscriberRepo.findBySubscriberAccountNumber(subscriberAccountNumber);
    // if (!clientOptional.isPresent()) {
    // response.put("timestamp", timestamp);
    // response.put("status", String.valueOf(HttpStatus.NOT_FOUND.value()));
    // response.put("message", "subscriber does not exist for account number: " +
    // subscriberAccountNumber);
    // return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    // }

    // subscriberEntity client = clientOptional.get();

    // // Check if the subscriber is deactivated
    // if (client.getSubsStatus() == null ||
    // (!client.getSubsStatus().equals("DEACTIVATED"))) {
    // response.put("timestamp", timestamp);
    // response.put("status", String.valueOf(HttpStatus.CONFLICT.value()));
    // response.put("message", "subscriber not Inactive (need to deactivate account
    // first)");
    // return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    // }

    // // Get the serial number from the client
    // String serialNumber = client.getOnuSerialNumber();
    // if (serialNumber == null) {
    // response.put("timestamp", timestamp);
    // response.put("status",
    // String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
    // response.put("message", "Serial number is not available for account number: "
    // + subscriberAccountNumber);
    // return
    // ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    // }

    // // Call to ACS to REMOVE WAN2
    // String apiUrl = acsApiUrl + "deleteWanInstance";
    // HttpHeaders headers = new HttpHeaders();
    // headers.setContentType(MediaType.APPLICATION_JSON);

    // String instance = "2";

    // // Create a JSON request body
    // StringBuilder jsonBody = new StringBuilder();
    // jsonBody.append("{");
    // jsonBody.append("\"serialNumber\":\"").append(serialNumber).append("\",");
    // jsonBody.append("\"Instance\":\"").append(instance).append("\"");
    // jsonBody.append("}");

    // String jsonRequestBody = jsonBody.toString();
    // System.out.println(jsonRequestBody);

    // HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody,
    // headers);
    // RestTemplate restTemplate = new RestTemplate();
    // String jsonResponse = restTemplate.postForObject(apiUrl, requestEntity,
    // String.class);

    // System.out.println("HiveConnect: account terminated Successful");
    // System.out.println("Response: " + jsonResponse);

    // // Handle response and update client status
    // if (jsonResponse.contains("Successful")) {
    // // Update client status to 'terminated'
    // client.setSubsStatus("TERMINATED");
    // subscriberRepo.save(client);

    // response.put("timestamp", timestamp);
    // response.put("status", String.valueOf(HttpStatus.OK.value()));
    // response.put("message", "HiveConnect: account terminated Successful");
    // return ResponseEntity.status(HttpStatus.OK).body(response);
    // } else {
    // response.put("timestamp", timestamp);
    // response.put("status",
    // String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
    // response.put("message", jsonResponse);
    // return
    // ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    // }
    // }

    // @Async("AsyncExecutor")
    // @PostMapping("/terminateSubscriber")
    // public ResponseEntity<Map<String, String>>
    // permanentDisconnectClient(@RequestBody Map<String, String> params) {
    // // Extract account number from request body
    // String subscriberAccountNumber = params.get("subscriberAccountNumber");

    // // Fetch client from repository
    // Optional<HiveClient> clientOptional =
    // hiveClientRepository.findBySubscriberAccountNumber(subscriberAccountNumber);
    // if (!clientOptional.isPresent()) {
    // return ResponseEntity.status(HttpStatus.NOT_FOUND)
    // .body(Collections.singletonMap("message", "Client not found for account
    // number: " + subscriberAccountNumber));
    // }

    // HiveClient client = clientOptional.get();

    // // Get the serial number from the client
    // String serialNumber = client.getOnuSerialNumber();

    // // Call to ACS to REMOVE WAN2
    // String apiUrl = acsApiUrl + "deleteWanInstance";

    // // Create headers with Content-Type set to application/json
    // HttpHeaders headers = new HttpHeaders();
    // headers.setContentType(MediaType.APPLICATION_JSON);

    // String instance = "2";

    // // Create a JSON request body
    // StringBuilder jsonBody = new StringBuilder();
    // jsonBody.append("{");
    // jsonBody.append("\"serialNumber\":\"").append(serialNumber).append("\",");
    // jsonBody.append("\"Instance\":\"").append(instance).append("\"");
    // jsonBody.append("}");

    // String jsonRequestBody = jsonBody.toString();
    // System.out.println(jsonRequestBody);

    // HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody,
    // headers);
    // RestTemplate restTemplate = new RestTemplate();
    // String jsonResponse = restTemplate.postForObject(apiUrl, requestEntity,
    // String.class);

    // System.out.println("HiveConnect: ACS Push: WAN2 Delete Task Pushed");
    // System.out.println("Response: " + jsonResponse);

    // // Handle response and update client status
    // if (jsonResponse.contains("Successful")) {
    // // Update client status to 'terminated'
    // client.setStatus("terminated");
    // hiveClientRepository.save(client);

    // Map<String, String> response = new HashMap<>();
    // response.put("status", "200");
    // response.put("message", "HiveConnect: ACS Push: WAN2 Delete Task Pushed");
    // return ResponseEntity.status(HttpStatus.OK).body(response);
    // } else {
    // Map<String, String> response = new HashMap<>();
    // response.put("status", "500");
    // response.put("message", "HiveConnect: ACS Push: WAN2 Delete Task Pushed");
    // return
    // ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    // }
    // }
    // ----------------end for permanent disconnection

    // ]]]]]]---------------Exposed APIs for Connect-Disconnect

    // [[[[[[-------------- Controller Functions ---- On ERRORS

    // Rollback WAN2, delete WAN2. Applicable after succeeding errors

    static String deleteWanInstance(String serialNumber) {
        String apiUrl = acsApiUrl + "deleteWanInstance";

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
        String apiUrl = acsApiUrl + "rollbackSsid";

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

        System.out.println("HiveConnect: ACS Server Rolled Back SSID for  " + serialNumber);
        System.out.println("Response: " + jsonResponse);

        return "HiveConnect: ACS Server SSID Rollback pushed for " + serialNumber;
    }

    // ]]]]]]-------------- Controller Functions ---- On ERRORS

    // [[[[[[-------------- Controller Functions ---- On SUCCESS

    // Unrogue ONU on ACS
    public static String onuOnboarded(String serialNumber) {
        String apiUrl = acsApiUrl + "onuOnboarded";

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
        String apiUrl = acsApiUrl + "setInformInterval";

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
    // ]]]]]]-------------- Controller Functions ---- On SUCCESS

    // [[[[[[-------------- Database Interactions

    public String setParent(String parent, String serialNumber) {

        DeviceRepo.updateParentBySerialNumber(parent, serialNumber);
        return "Successful";
    }

    public static String getWan2MacAddress(String serialNumber) {
        String apiUrl = acsApiUrl + "getWan2MacAddress";

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

        System.out.println("HiveConnect: Saving WAN2 Mac Address of  " + serialNumber);
        System.out.println("Response: " + jsonResponse);

        return "HiveConnect: Saving WAN2 Mac Address of " + serialNumber;
    }

    // ]]]]]]-------------- Database Interactions

}