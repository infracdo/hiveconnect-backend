package com.autoprov.autoprov.controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.autoprov.autoprov.entity.acsDomain.Device;
import com.autoprov.autoprov.entity.subscriberDomain.subscriberEntity;
import com.autoprov.autoprov.repositories.acsRepositories.DeviceRepository;
import com.autoprov.autoprov.repositories.hiveRepositories.HiveClientRepository;
import com.autoprov.autoprov.repositories.subscriberRepositories.subscriberRepository;




@CrossOrigin(origins = "*")
@RestController
public class AcsController {

    @Autowired
    private DeviceRepository DeviceRepo;

    @Autowired
    private HiveClientRepository hiveClientRepository;

    @Autowired
    private subscriberRepository subscriberRepo;

    

    private static String acsApiUrl = "http://192.168.90.101:7547/";

    // Exposed for HiveApp ----------------------------------------
    @Async("AsyncExecutor")
    @GetMapping("/getRogueDevices")
    // @PreAuthorize("hasAuthority('HIVECONNECT_ROGUE_DEVICES_READ')")
    public CompletableFuture<List<Device>> getRougeDevices() {

        List<Device> Device = new ArrayList<>();
        DeviceRepo.findByGroup("unassigned").forEach(Device::add);
        System.out.println("backend hive api accessed");
        return CompletableFuture.completedFuture(Device);

        // String apiUrl = acsApiUrl + "getRogueDevices";

        // RestTemplate restTemplate = new RestTemplate();
        // List response = restTemplate.getForObject(apiUrl, List.class);

        // return ResponseEntity.status(HttpStatus.OK).body(response);

    }

    // Exposed for HiveApp (end) ----------------------------------------

    // [[[[[[---------------Exposed APIs for Connect-Disconnect  [REQUIRES AUTH AND TESTING]
   
//--------------deactivateSubscriber base on accountNumber---------------------  [USED FOR BILLING]
@Async("AsyncExecutor")
@PostMapping("/deactivateSubscriber")
// @PreAuthorize("hasAuthority('HIVECONNECT_API_BILLING_ACCESS')")
public ResponseEntity<Map<String, String>> disconnectClient(@RequestBody Map<String, String> params) {
    Map<String, String> response = new LinkedHashMap<>(); // Use String as the value type
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    String subscriberAccountNumber = params.get("subscriberAccountNumber");

    // Check if the subscriber account number is empty or null
    if (subscriberAccountNumber == null || subscriberAccountNumber.isEmpty()) {
        response.put("timestamp", timestamp);
        response.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
        response.put("message", "subscriber account number is empty");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Fetch the client from the repository based on the account number
    Optional<subscriberEntity> optionalClient = subscriberRepo.findBySubscriberAccountNumber(subscriberAccountNumber);
    if (!optionalClient.isPresent()) {
        response.put("timestamp", timestamp);
        response.put("status", String.valueOf(HttpStatus.CONFLICT.value()));
        response.put("message", "subscriber account number does not exist: " + subscriberAccountNumber);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    subscriberEntity client = optionalClient.get();

    // Check if the subscriber is active
    // if (client.getSubsStatus() == null || !client.getSubsStatus().equals("ACTIVE")|| !client.getSubsStatus().equals("Activated")) {
    //     response.put("timestamp", timestamp);
    //     response.put("status", String.valueOf(HttpStatus.CONFLICT.value()));
    //     response.put("message", "subscriber not active");
    //     return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    // }
    if (client.getSubsStatus() == null || 
   (!client.getSubsStatus().equals("ACTIVE") && !client.getSubsStatus().equals("Activated"))) {
    response.put("timestamp", timestamp);
    response.put("status", String.valueOf(HttpStatus.CONFLICT.value()));
    response.put("message", "subscriber not active");
    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
}

    // Get the serial number from the client
    String serialNumber = client.getOnuSerialNumber();
    if (serialNumber == null) {
        response.put("timestamp", timestamp);
        response.put("status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
        response.put("message", "Serial number is not available for account number: " + subscriberAccountNumber);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // Call to ACS to Disconnect Wan2
    String apiUrl = acsApiUrl + "toggleWan";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    String instance = "2";
    String toggle = "0";

    // Create a JSON request body
    StringBuilder jsonBody = new StringBuilder();
    jsonBody.append("{");
    jsonBody.append("\"serialNumber\":\"").append(serialNumber).append("\",");
    jsonBody.append("\"Instance\":\"").append(instance).append("\",");
    jsonBody.append("\"Toggle\":\"").append(toggle).append("\"");
    jsonBody.append("}");

    String jsonRequestBody = jsonBody.toString();
    System.out.println(jsonRequestBody);

    HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody, headers);
    RestTemplate restTemplate = new RestTemplate();
    String jsonResponse = restTemplate.postForObject(apiUrl, requestEntity, String.class);

    System.out.println("HiveConnect Pushed: subscriber successfully deactivated");
    System.out.println("Response: " + jsonResponse);

    // Handle the response and update the client status
    if (jsonResponse.contains("Pushed")) {
        client.setSubsStatus("DEACTIVATED");
        subscriberRepo.save(client);

        response.put("timestamp", timestamp);
        response.put("status", String.valueOf(HttpStatus.CREATED.value()));
        response.put("message", "subscriber successfully deactivated");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } else {
        response.put("timestamp", timestamp);
        response.put("status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
        response.put("message", jsonResponse);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}


//-------POST END POINT BASE ON ACCOUNT NUMBER SEND FOR TEMPORARY DISCONNECTION
// @Async("AsyncExecutor")
// @PostMapping("/deactivateSubscriber")
// public ResponseEntity<Map<String, String>> disconnectClient(@RequestBody Map<String, String> params) {
//     // Extract account number from request body
//     String accountNumber = params.get("accountNumber");
    
//     // Fetch client from repository
//     Optional<HiveClient> optionalClient = hiveClientRepository.findBySubscriberAccountNumber(accountNumber);
//     if (!optionalClient.isPresent()) {
//         return ResponseEntity.status(HttpStatus.NOT_FOUND)
//             .body(Collections.singletonMap("message", "Client not found for account number: " + accountNumber));
//     }

//     HiveClient client = optionalClient.get();

//     // Get the serial number from the client
//     String serialNumber = client.getOnuSerialNumber();
//     if (serialNumber == null) {
//         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//             .body(Collections.singletonMap("message", "Serial number is not available for account number: " + accountNumber));
//     }

//     // Call to ACS to Disconnect Wan2
//     String apiUrl = acsApiUrl + "toggleWan";

//     // Create headers with Content-Type set to application/json
//     HttpHeaders headers = new HttpHeaders();
//     headers.setContentType(MediaType.APPLICATION_JSON);

//     String instance = "2";
//     String toggle = "0";

//     // Create a JSON request body
//     StringBuilder jsonBody = new StringBuilder();
//     jsonBody.append("{");
//     jsonBody.append("\"serialNumber\":\"").append(serialNumber).append("\",");
//     jsonBody.append("\"Instance\":\"").append(instance).append("\",");
//     jsonBody.append("\"Toggle\":\"").append(toggle).append("\"");
//     jsonBody.append("}");

//     String jsonRequestBody = jsonBody.toString();
//     System.out.println(jsonRequestBody);

//     HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody, headers);
//     RestTemplate restTemplate = new RestTemplate();
//     String jsonResponse = restTemplate.postForObject(apiUrl, requestEntity, String.class);

//     System.out.println("HiveConnect: ACS Push: WAN2 Disable Task Pushed");
//     System.out.println("Response: " + jsonResponse);

//     // Handle response and update client status
//     if (jsonResponse.contains("Pushed")) {
//         // Update client status to 'deactivated'
//         client.setStatus("deactivated");
//         hiveClientRepository.save(client);

//         Map<String, String> response = new HashMap<>();
//         response.put("status", "200");
//         response.put("message", "HiveConnect: ACS Push: WAN2 Disable Task Pushed");
//         return ResponseEntity.status(HttpStatus.OK).body(response);
//     } else {
//         Map<String, String> response = new HashMap<>();
//         response.put("status", "500");
//         response.put("message", jsonResponse);
//         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//     }
// }

   
//----------------Activate/Reconnect Subscriber  [USED FOR BILLING]
@Async("AsyncExecutor")
@PostMapping("/activateSubscriber")
// @PreAuthorize("hasAuthority('HIVECONNECT_API_BILLING_ACCESS')")
public ResponseEntity<Map<String, String>> reconnectClient(@RequestBody Map<String, String> params) {
    Map<String, String> response = new LinkedHashMap<>();
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    String subscriberAccountNumber = params.get("subscriberAccountNumber");

    // Check if the subscriber account number is empty or null
    if (subscriberAccountNumber == null || subscriberAccountNumber.isEmpty()) {
        response.put("timestamp", timestamp);
        response.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
        response.put("message", "subscriber account number is empty");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Fetch client from repository
    Optional<subscriberEntity> clientOptional = subscriberRepo.findBySubscriberAccountNumber(subscriberAccountNumber);
    if (!clientOptional.isPresent()) {
        response.put("timestamp", timestamp);
        response.put("status", String.valueOf(HttpStatus.NOT_FOUND.value()));
        response.put("message", "subscriber does not exist for account number: " + subscriberAccountNumber);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    subscriberEntity client = clientOptional.get();


    // Check if the subscriber is deactivated
    if (client.getSubsStatus() == null || !client.getSubsStatus().equals("DEACTIVATED")) {
        response.put("timestamp", timestamp);
        response.put("status", String.valueOf(HttpStatus.CONFLICT.value()));
        response.put("message", "subscriber not Inactive");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // Get the serial number from the client
    String serialNumber = client.getOnuSerialNumber();
    if (serialNumber == null) {
        response.put("timestamp", timestamp);
        response.put("status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
        response.put("message", "Serial number is not available for account number: " + subscriberAccountNumber);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // Call to ACS to Reconnect Wan2
    String apiUrl = acsApiUrl + "toggleWan";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    String instance = "2";
    String toggle = "1"; // Toggle value for reconnect

    // Create a JSON request body
    StringBuilder jsonBody = new StringBuilder();
    jsonBody.append("{");
    jsonBody.append("\"serialNumber\":\"").append(serialNumber).append("\",");
    jsonBody.append("\"Instance\":\"").append(instance).append("\",");
    jsonBody.append("\"Toggle\":\"").append(toggle).append("\"");
    jsonBody.append("}");

    String jsonRequestBody = jsonBody.toString();
    System.out.println(jsonRequestBody);

    HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody, headers);
    RestTemplate restTemplate = new RestTemplate();
    String jsonResponse = restTemplate.postForObject(apiUrl, requestEntity, String.class);

    System.out.println("HiveConnect Pushed:subscriber successfully activated");
    System.out.println("Response: " + jsonResponse);

    // Handle response
    if (jsonResponse.contains("Pushed")) {
        // Update client status to 'active'
        client.setSubsStatus("ACTIVE");
        subscriberRepo.save(client);

        response.put("timestamp", timestamp);
        response.put("status", String.valueOf(HttpStatus.OK.value()));
        response.put("message", "HiveConnect:subscriver successfully activated");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    } else {
        response.put("timestamp", timestamp);
        response.put("status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
        response.put("message", jsonResponse);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}


// POST end point to Recconect Internet based on account NUmber sent from billing
// @Async("AsyncExecutor")
// @PostMapping("/activateSubscriber")
// public ResponseEntity<Map<String, String>> reconnectClient(@RequestBody Map<String, String> params) {
//     // Extract account number from request body
//     String subscriberAccountNumber = params.get("subscriberAccountNumber");
    
//     // Fetch client from repository
//     Optional<HiveClient> clientOptional = hiveClientRepository.findBySubscriberAccountNumber(subscriberAccountNumber);
//     if (!clientOptional.isPresent()) {
//         return ResponseEntity.status(HttpStatus.NOT_FOUND)
//             .body(Collections.singletonMap("message", "Client not found for account number: " + subscriberAccountNumber));
//     }
    
//     HiveClient client = clientOptional.get();

//     // Get the serial number from the client
//     String serialNumber = client.getOnuSerialNumber();

//     // Call to ACS to Reconnect Wan2
//     String apiUrl = acsApiUrl + "toggleWan";

//     // Create headers with Content-Type set to application/json
//     HttpHeaders headers = new HttpHeaders();
//     headers.setContentType(MediaType.APPLICATION_JSON);

//     String instance = "2";
//     String toggle = "1"; // Toggle value for reconnect

//     // Create a JSON request body
//     StringBuilder jsonBody = new StringBuilder();
//     jsonBody.append("{");
//     jsonBody.append("\"serialNumber\":\"").append(serialNumber).append("\",");
//     jsonBody.append("\"Instance\":\"").append(instance).append("\",");
//     jsonBody.append("\"Toggle\":\"").append(toggle).append("\"");
//     jsonBody.append("}");

//     String jsonRequestBody = jsonBody.toString();
//     System.out.println(jsonRequestBody);

//     HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody, headers);
//     RestTemplate restTemplate = new RestTemplate();
//     String jsonResponse = restTemplate.postForObject(apiUrl, requestEntity, String.class);

//     System.out.println("HiveConnect: ACS Push: WAN2 Enable Task Pushed");
//     System.out.println("Response: " + jsonResponse);

//     // Handle response
//     if (jsonResponse.contains("Pushed")) {
//         // Update client status to 'active'
//         client.setStatus("active");
//         hiveClientRepository.save(client);

//         Map<String, String> response = new HashMap<>();
//         response.put("status", "200");
//         response.put("message", "HiveConnect: ACS Push: WAN2 Enable Task Pushed");
//         return ResponseEntity.status(HttpStatus.OK).body(response);
//     } else {
//         Map<String, String> response = new HashMap<>();
//         response.put("status", "500");
//         response.put("message", jsonResponse);
//         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//     }
// }

 //----------------UPDATE PACKAGE----no function yet  [USED FOR BILLING]
 @Async("AsyncExecutor")
 @PostMapping("/updateSubscriberPackage")
 // @PreAuthorize("hasAuthority('HIVECONNECT_API_BILLING_ACCESS')")
 public ResponseEntity<Map<String, String>> updateSubscriberPackage(@RequestBody Map<String, String> params) {
     Map<String, String> response = new LinkedHashMap<>();
     String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
     String subscriberAccountNumber = params.get("subscriberAccountNumber");
     String packageType = params.get("packageType");
 
     // Check if the subscriber account number is empty or null
     if (subscriberAccountNumber == null || subscriberAccountNumber.isEmpty()) {
         response.put("timestamp", timestamp);
         response.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
         response.put("message", "Subscriber account number is empty");
         return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
     }
 
     // Check if the package type is empty or null
     if (packageType == null || packageType.isEmpty()) {
         response.put("timestamp", timestamp);
         response.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
         response.put("message", "Package type is empty");
         return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
     }
 
     // // Fetch package from repository
     // Optional<subscriberEntity> packageOptional = subscriberRepo.findByPackageType(packageType);
     // if (!packageOptional.isPresent()) {
     //     response.put("timestamp", timestamp);
     //     response.put("status", String.valueOf(HttpStatus.NOT_FOUND.value()));
     //     response.put("message", "Package does not exist for type: " + packageType);
     //     return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
     // }
 
     // Fetch client from repository
     Optional<subscriberEntity> clientOptional = subscriberRepo.findBySubscriberAccountNumber(subscriberAccountNumber);
     if (!clientOptional.isPresent()) {
         response.put("timestamp", timestamp);
         response.put("status", String.valueOf(HttpStatus.NOT_FOUND.value()));
         response.put("message", "subscriber account number does not exist ");
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
 
         // Prepare success response
         response.put("timestamp", timestamp);
         response.put("status", String.valueOf(HttpStatus.OK.value()));
         response.put("message", "subscriber package successfully updated");
 
         return ResponseEntity.status(HttpStatus.OK).body(response);
     } catch (Exception e) {
         // Handle any unexpected exceptions
         response.put("timestamp", timestamp);
         response.put("status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
         response.put("message", "An unexpected error occurred: " + e.getMessage());
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
     }
 }


//----------------UPDATE PROVISION STATUS---no function yet  [USED FOR BILLING]
@Async("AsyncExecutor")
@PostMapping("/updateSubscriberProvision")
// @PreAuthorize("hasAuthority('HIVECONNECT_API_BILLING_ACCESS')")
public ResponseEntity<Map<String, String>> updateSubscriberProvision(@RequestBody Map<String, String> params) {
   Map<String, String> response = new LinkedHashMap<>();
   String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
   String subscriberAccountNumber = params.get("subscriberAccountNumber");
   String provision = params.get("provision");

   // Check if the subscriber account number is empty or null
   if (subscriberAccountNumber == null || subscriberAccountNumber.isEmpty()) {
       response.put("timestamp", timestamp);
       response.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
       response.put("message", "Subscriber account number is empty");
       return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
   }

   // Check if the provision is empty or null
   if (provision == null || provision.isEmpty()) {
       response.put("timestamp", timestamp);
       response.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
       response.put("message", "Provision is empty");
       return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
   }

   // Validate provision type
   String provisionUpperCase = provision.toUpperCase();
   if (!(provisionUpperCase.equals("HIVECONNECT") || provisionUpperCase.equals("HIVE") || provisionUpperCase.equals("BUCKET"))) {
       response.put("timestamp", timestamp);
       response.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
       response.put("message", "Provision type does not exist");
       return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
   }

   // Fetch subscriber from repository
   Optional<subscriberEntity> clientOptional = subscriberRepo.findBySubscriberAccountNumber(subscriberAccountNumber);
   if (!clientOptional.isPresent()) {
       response.put("timestamp", timestamp);
       response.put("status", String.valueOf(HttpStatus.NOT_FOUND.value()));
       response.put("message", "Subscriber does not exist for account number: " + subscriberAccountNumber);
       return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
   }

   try {
       // Get the client entity
       subscriberEntity client = clientOptional.get();
       String currentProvision = client.getProvision();
       String subsStatus = client.getSubsStatus();

       // Check if provision contains specific words, ignoring case
       if (provisionUpperCase.contains("HIVECONNECT") || provisionUpperCase.contains("HIVE")) {
           if ("NEW".equalsIgnoreCase(subsStatus)) {
               response.put("timestamp", timestamp);
               response.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
               response.put("message", "This account number is not yet provisioned");
               return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
           } else {
               client.setProvision("HIVECONNECT");
           }
       } else if ("BUCKET".equalsIgnoreCase(provision)) {
           if ("BUCKET".equalsIgnoreCase(subsStatus)) {
               client.setProvision("BUCKET");
               subscriberRepo.save(client);
               response.put("timestamp", timestamp);
               response.put("status", String.valueOf(HttpStatus.OK.value()));
               response.put("message", "Subscriber provision successfully updated to BUCKET");
               return ResponseEntity.status(HttpStatus.OK).body(response);
           } else {
               response.put("timestamp", timestamp);
               response.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
               response.put("message", "This account number is not yet provisioned");
               return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
           }
       }

       // Save the updated client entity
       subscriberRepo.save(client);

       // Prepare success response
       response.put("timestamp", timestamp);
       response.put("status", String.valueOf(HttpStatus.OK.value()));
       response.put("message", "subscriber provision status successfully updated");
       return ResponseEntity.status(HttpStatus.OK).body(response);

   } catch (Exception e) {
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
// @PreAuthorize("hasAuthority('HIVECONNECT_API_BILLING_ACCESS')")
public ResponseEntity<Map<String, String>> permanentDisconnectClient(@RequestBody Map<String, String> params) {
    Map<String, String> response = new LinkedHashMap<>();
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    String subscriberAccountNumber = params.get("subscriberAccountNumber");

    if (subscriberAccountNumber == null || subscriberAccountNumber.isEmpty()) {
        response.put("timestamp", timestamp);
        response.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
        response.put("message", "Subscriber account number is empty");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    Optional<subscriberEntity> clientOptional = subscriberRepo.findBySubscriberAccountNumber(subscriberAccountNumber);
    if (!clientOptional.isPresent()) {
        response.put("timestamp", timestamp);
        response.put("status", String.valueOf(HttpStatus.NOT_FOUND.value()));
        response.put("message", "Subscriber does not exist for account number: " + subscriberAccountNumber);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    subscriberEntity client = clientOptional.get();

    if (client.getSubsStatus() == null || !client.getSubsStatus().equals("DEACTIVATED")) {
        response.put("timestamp", timestamp);
        response.put("status", String.valueOf(HttpStatus.CONFLICT.value()));
        response.put("message", "Subscriber not inactive (need to deactivate account first)");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    String serialNumber = client.getOnuSerialNumber();
    if (serialNumber == null) {
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

        if (jsonResponse != null && jsonResponse.contains("Successful")) {
            client.setSubsStatus("TERMINATED");
            subscriberRepo.save(client);

            response.put("timestamp", timestamp);
            response.put("status", String.valueOf(HttpStatus.OK.value()));
            response.put("message", "HiveConnect: account terminated successfully");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            response.put("timestamp", timestamp);
            response.put("status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            response.put("message", jsonResponse != null ? jsonResponse : "Unknown error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    } catch (Exception e) {
        response.put("timestamp", timestamp);
        response.put("status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
        response.put("message", "Exception occurred: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

//  @Async("AsyncExecutor")
//  @PostMapping("/terminateSubscriber")
//  public ResponseEntity<Map<String, String>> permanentDisconnectClient(@RequestBody Map<String, String> params) {
//      Map<String, String> response = new LinkedHashMap<>();
//      String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//      String subscriberAccountNumber = params.get("subscriberAccountNumber");
 
//      // Check if the subscriber account number is empty or null
//      if (subscriberAccountNumber == null || subscriberAccountNumber.isEmpty()) {
//          response.put("timestamp", timestamp);
//          response.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
//          response.put("message", "subscriber account number is empty");
//          return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//      }
 
//      // Fetch client from repository
//      Optional<subscriberEntity> clientOptional = subscriberRepo.findBySubscriberAccountNumber(subscriberAccountNumber);
//      if (!clientOptional.isPresent()) {
//          response.put("timestamp", timestamp);
//          response.put("status", String.valueOf(HttpStatus.NOT_FOUND.value()));
//          response.put("message", "subscriber does not exist for account number: " + subscriberAccountNumber);
//          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//      }
 
//      subscriberEntity client = clientOptional.get();
 
//      // Check if the subscriber is deactivated
//      if (client.getSubsStatus() == null || (!client.getSubsStatus().equals("DEACTIVATED"))) {
//          response.put("timestamp", timestamp);
//          response.put("status", String.valueOf(HttpStatus.CONFLICT.value()));
//          response.put("message", "subscriber not Inactive (need to deactivate account first)");
//          return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
//      }
 
//      // Get the serial number from the client
//      String serialNumber = client.getOnuSerialNumber();
//      if (serialNumber == null) {
//          response.put("timestamp", timestamp);
//          response.put("status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
//          response.put("message", "Serial number is not available for account number: " + subscriberAccountNumber);
//          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//      }
 
//      // Call to ACS to REMOVE WAN2
//      String apiUrl = acsApiUrl + "deleteWanInstance";
//      HttpHeaders headers = new HttpHeaders();
//      headers.setContentType(MediaType.APPLICATION_JSON);
 
//      String instance = "2";
 
//      // Create a JSON request body
//      StringBuilder jsonBody = new StringBuilder();
//      jsonBody.append("{");
//      jsonBody.append("\"serialNumber\":\"").append(serialNumber).append("\",");
//      jsonBody.append("\"Instance\":\"").append(instance).append("\"");
//      jsonBody.append("}");
 
//      String jsonRequestBody = jsonBody.toString();
//      System.out.println(jsonRequestBody);
 
//      HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody, headers);
//      RestTemplate restTemplate = new RestTemplate();
//      String jsonResponse = restTemplate.postForObject(apiUrl, requestEntity, String.class);
 
//      System.out.println("HiveConnect: account terminated Successful");
//      System.out.println("Response: " + jsonResponse);
 
//      // Handle response and update client status
//      if (jsonResponse.contains("Successful")) {
//          // Update client status to 'terminated'
//          client.setSubsStatus("TERMINATED");
//          subscriberRepo.save(client);
 
//          response.put("timestamp", timestamp);
//          response.put("status", String.valueOf(HttpStatus.OK.value()));
//          response.put("message", "HiveConnect: account terminated Successful");
//          return ResponseEntity.status(HttpStatus.OK).body(response);
//      } else {
//          response.put("timestamp", timestamp);
//          response.put("status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
//          response.put("message", jsonResponse);
//          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//      }
//  }
 

// @Async("AsyncExecutor")
// @PostMapping("/terminateSubscriber")
// public ResponseEntity<Map<String, String>> permanentDisconnectClient(@RequestBody Map<String, String> params) {
//     // Extract account number from request body
//     String subscriberAccountNumber = params.get("subscriberAccountNumber");

//     // Fetch client from repository
//     Optional<HiveClient> clientOptional = hiveClientRepository.findBySubscriberAccountNumber(subscriberAccountNumber);
//     if (!clientOptional.isPresent()) {
//         return ResponseEntity.status(HttpStatus.NOT_FOUND)
//             .body(Collections.singletonMap("message", "Client not found for account number: " + subscriberAccountNumber));
//     }

//     HiveClient client = clientOptional.get();

//     // Get the serial number from the client
//     String serialNumber = client.getOnuSerialNumber();

//     // Call to ACS to REMOVE WAN2
//     String apiUrl = acsApiUrl + "deleteWanInstance";

//     // Create headers with Content-Type set to application/json
//     HttpHeaders headers = new HttpHeaders();
//     headers.setContentType(MediaType.APPLICATION_JSON);

//     String instance = "2";

//     // Create a JSON request body
//     StringBuilder jsonBody = new StringBuilder();
//     jsonBody.append("{");
//     jsonBody.append("\"serialNumber\":\"").append(serialNumber).append("\",");
//     jsonBody.append("\"Instance\":\"").append(instance).append("\"");
//     jsonBody.append("}");

//     String jsonRequestBody = jsonBody.toString();
//     System.out.println(jsonRequestBody);

//     HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody, headers);
//     RestTemplate restTemplate = new RestTemplate();
//     String jsonResponse = restTemplate.postForObject(apiUrl, requestEntity, String.class);

//     System.out.println("HiveConnect: ACS Push: WAN2 Delete Task Pushed");
//     System.out.println("Response: " + jsonResponse);

//     // Handle response and update client status
//     if (jsonResponse.contains("Successful")) {
//         // Update client status to 'terminated'
//         client.setStatus("terminated");
//         hiveClientRepository.save(client);

//         Map<String, String> response = new HashMap<>();
//         response.put("status", "200");
//         response.put("message", "HiveConnect: ACS Push: WAN2 Delete Task Pushed");
//         return ResponseEntity.status(HttpStatus.OK).body(response);
//     } else {
//         Map<String, String> response = new HashMap<>();
//         response.put("status", "500");
//         response.put("message", "HiveConnect: ACS Push: WAN2 Delete Task Pushed");
//         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//     }
// }
//----------------end for permanent disconnection


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