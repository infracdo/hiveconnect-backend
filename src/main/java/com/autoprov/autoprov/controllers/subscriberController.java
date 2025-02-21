package com.autoprov.autoprov.controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.autoprov.autoprov.entity.hiveDomain.HiveClient;
import com.autoprov.autoprov.entity.subscriberDomain.subscriberEntity;
import com.autoprov.autoprov.repositories.hiveRepositories.HiveClientRepository;
import com.autoprov.autoprov.services.HiveClientService;
import com.autoprov.autoprov.services.subscriberService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@CrossOrigin(origins = "*")
@RestController
public class subscriberController {

    @Autowired
    private final subscriberService SubscriberService;

    @Autowired
    private final HiveClientService hiveclientService;

    @Autowired
    private HiveClientRepository hiveClientRepository;

    // POST END POINT add or create new subscriber endpoint
    public subscriberController(subscriberService SubscriberService, HiveClientService hiveclientService) {
        this.SubscriberService = SubscriberService;
        this.hiveclientService = hiveclientService;
    }

    // EXPOSE THIS API [USED FOR BILLING]
    @Async("asyncExecutor")
    @PostMapping("/createSubscriberForProvisioning")
    @PreAuthorize("hasAuthority('HIVECONNECT_API_BILLING_ACCESS')")
    public ResponseEntity<?> addSubscriberForProvisioning(@Valid @RequestBody subscriberEntity subscriberEntity) {
        try {
            // Check if the account number is empty
            if (subscriberEntity.getSubscriberAccountNumber() == null
                    || subscriberEntity.getSubscriberAccountNumber().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse(HttpStatus.BAD_REQUEST, "subscriber account number is empty"));
            }

            // Check if the subscriber name is empty or too long
            if (subscriberEntity.getSubscriberName() == null || subscriberEntity.getSubscriberName().trim().isEmpty()
                    || subscriberEntity.getSubscriberName().length() > 50) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse(HttpStatus.BAD_REQUEST, "subscriber name is empty"));
            }

            // Set status to NEW
            subscriberEntity.setSubsStatus("NEW");

            subscriberEntity savedSubscriber = SubscriberService.saveSubscriber(subscriberEntity);
            return ResponseEntity.status(HttpStatus.CREATED).body(createSuccessResponse());
        } catch (SubscriberAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse(HttpStatus.UNAUTHORIZED, "subscriber account number already exist"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse(HttpStatus.CONFLICT, "Error saving the account: " + e.getMessage()));
        }
    }

    // EXPOSE THIS API [USED FOR CLIENT MIGRATION]
    @Async("asyncExecutor")
    @PostMapping("/createSubscriberForMigration")
    @PreAuthorize("hasAuthority('HIVECONNECT_CLIENT_MIGRATION_ACTION')")
    public ResponseEntity<?> addSubscriberForMigration(@Valid @RequestBody HiveClient hiveClient) {
        try {
            // Check if the account number is empty
            if (hiveClient.getSubscriberAccountNumber() == null
                    || hiveClient.getSubscriberAccountNumber().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse(HttpStatus.BAD_REQUEST,
                                "subscriberAccountNumber (account_no) is missing/invalid"));
            }

            if (hiveClient.getProvision() == null || hiveClient.getProvision().trim().isEmpty()
                    || hiveClient.getProvision().length() > 50) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse(HttpStatus.BAD_REQUEST, "provision (backend) is missing/invalid"));
            }

            // Check if the subscriber name is empty or too long
            if (hiveClient.getClientName() == null || hiveClient.getClientName().trim().isEmpty()
                    || hiveClient.getClientName().length() > 50) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse(HttpStatus.BAD_REQUEST,
                                "clientName (client_name) is missing/invalid"));
            }

            if (hiveClient.getOltReportedUpstream() == null || hiveClient.getOltReportedUpstream().trim().isEmpty()
                    || hiveClient.getOltReportedUpstream().length() > 50) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse(HttpStatus.BAD_REQUEST,
                                "oltReportedUpstream (olt_upstream) is missing/invalid"));
            }

            if (hiveClient.getOltReportedDownstream() == null || hiveClient.getOltReportedDownstream().trim().isEmpty()
                    || hiveClient.getOltReportedDownstream().length() > 50) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse(HttpStatus.BAD_REQUEST,
                                "oltReportedDownstream (olt_downstream) is missing/invalid"));
            }

            if (hiveClient.getOnuDeviceName() == null || hiveClient.getOnuDeviceName().trim().isEmpty()
                    || hiveClient.getOnuDeviceName().length() > 50) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse(HttpStatus.BAD_REQUEST,
                                "onuDeviceName (subscription_name) is missing/invalid"));
            }

            if (hiveClient.getPackageType() == null || hiveClient.getPackageType().trim().isEmpty()
                    || hiveClient.getPackageType().length() > 50) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse(HttpStatus.BAD_REQUEST,
                                "packageType (package_type) is missing/invalid"));
            }

            if (hiveClient.getStatus() == null || hiveClient.getStatus().trim().isEmpty()
                    || hiveClient.getStatus().length() > 50) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse(HttpStatus.BAD_REQUEST, "status (status) is missing/invalid"));
            } else {
                hiveClient.setStatus(hiveClient.getStatus() + "_PENDING_MIGRATION");
            }

            HiveClientService.addHiveMigratedClient(hiveClient.getSubscriberAccountNumber(), hiveClient.getClientName(),
                    hiveClient.getOnuSerialNumber(), hiveClient.getOnuDeviceName(), hiveClient.getOnuMacAddress(),
                    hiveClient.getStatus(), hiveClient.getOltIp(), hiveClient.getOltInterface(),
                    hiveClient.getIpAssigned(),
                    hiveClient.getProvision(), hiveClient.getSsidName(),
                    hiveClient.getPackageType(), hiveClient.getOltReportedUpstream(),
                    hiveClient.getOltReportedDownstream());

            return ResponseEntity.status(HttpStatus.CREATED).body(createSuccessResponse());
        } catch (SubscriberAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse(HttpStatus.UNAUTHORIZED, "subscriber account number already exist"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse(HttpStatus.CONFLICT, "Error saving the account: " + e.getMessage()));
        }
    }

    // TODO; TEST THIS IN POSTMAN
    // EXPOSE THIS API [USED FOR CLIENT MIGRATION]
    @Async("asyncExecutor")
    @PostMapping("/updateSubscriberStatusAfterMigration")
    @PreAuthorize("hasAuthority('HIVECONNECT_CLIENT_MIGRATION_ACTION')")
    public ResponseEntity<?> updateMigratedSubscriberStatus(@RequestBody Map<String, String> params) {
        Map<String, String> response = new LinkedHashMap<>();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String subscriberAccountNumber = params.get("subscriberAccountNumber");

        // Check if the subscriber account number is empty or null
        if (subscriberAccountNumber == null
                || subscriberAccountNumber.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(HttpStatus.BAD_REQUEST,
                            "subscriberAccountNumber (account_no) is missing/invalid"));
        }

        // Fetch client from repository todo: change to hiveclient
        Optional<HiveClient> clientOptional = hiveClientRepository
                .findBySubscriberAccountNumber(subscriberAccountNumber);
        if (!clientOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(HttpStatus.NOT_FOUND,
                            "subscriber does not exist"));
        }

        try {
            // Get the client entity
            HiveClient client = clientOptional.get();

            if (client.getStatus() == null || client.getStatus().trim().isEmpty()
                    || client.getStatus().length() > 50) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse(HttpStatus.BAD_REQUEST, "status (status) is missing/invalid"));
            }

            if (client.getStatus().contains("_PENDING_MIGRATION")) {
                // Update the client entity with new status
                client.setStatus(client.getStatus().replace("_PENDING_MIGRATION", ""));

                // Optionally, update other relevant fields if necessary
                // Example: client.setUpdatedAt(LocalDateTime.now());

                // Save the updated client entity
                hiveClientRepository.save(client);

                response.put("timestamp", timestamp);
                response.put("status", String.valueOf(HttpStatus.OK.value()));
                response.put("message", "migrated subscriber status updated successfully");
                return ResponseEntity.status(HttpStatus.OK).body(response);
            } else {
                response.put("timestamp", timestamp);
                response.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
                response.put("message", "subscriber status cannot be updated");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            // Handle any unexpected exceptions
            response.put("timestamp", timestamp);
            response.put("status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            response.put("message", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldError().getDefaultMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse(HttpStatus.BAD_REQUEST, errorMessage));
    }

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Map<String, Object> createSuccessResponse() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now().format(DATE_TIME_FORMATTER));
        response.put("status", HttpStatus.CREATED.value());
        response.put("message", "subscriber successfully created");
        return response;
    }

    private Map<String, Object> createErrorResponse(HttpStatus status, String message) {
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().format(DATE_TIME_FORMATTER));
        errorResponse.put("status", status.value());
        errorResponse.put("message", message);
        return errorResponse;
    }

    // GET endpoint to retrieve all subscribers
    @Async("asyncExecutor")
    @GetMapping("/getsubscribers")
    @PreAuthorize("hasAuthority('HIVECONNECT_PROVISIONING_READ')")
    public ResponseEntity<List<subscriberEntity>> getAllSubscribers() {
        try {
            List<subscriberEntity> subscribers = SubscriberService.getAllSubscribers();
            return ResponseEntity.ok(subscribers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // DEBUGGING
    @Async("asyncExecutor")
    @GetMapping("/getprovisionedsubscribers")
    @PreAuthorize("hasAuthority('HIVECONNECT_PROVISIONED_READ')")
    public ResponseEntity<List<subscriberEntity>> getProvisionedSubscribers() {
        try {
            List<subscriberEntity> provisionedSubscribers = SubscriberService.getProvisionedSubscribers();
            return ResponseEntity.ok(provisionedSubscribers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // GET endpoint to retrieve subscriber by id
    @Async("asyncExecutor")
    @GetMapping("/getsubscriberbyid/{id}")
    public ResponseEntity<?> getSubscriberById(@PathVariable Long id) {
        try {
            subscriberEntity subscriber = SubscriberService.getSubscriberById(id);
            if (subscriber != null) {
                return ResponseEntity.ok(subscriber);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse(HttpStatus.NOT_FOUND, "Subscriber not found with ID: " + id));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error retrieving subscriber: " + e.getMessage()));
        }

    }

    // DEBUGGING
    // GET endpoint to retrieve subscriber by id
    @Async("asyncExecutor")
    @GetMapping("/getHiveClientById/{id}")
    public ResponseEntity<?> getHiveClientById(@PathVariable Long id) {
        try {
            HiveClient hive = hiveclientService.getHiveClientById(id);
            if (hive != null) {
                return ResponseEntity.ok(hive);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse(HttpStatus.NOT_FOUND, "Subscriber not found with ID: " + id));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error retrieving subscriber: " + e.getMessage()));
        }
    }

    // GET endpoint to retrieve all hiveclients
    @Async("asyncExecutor")
    @GetMapping("/getHiveClients")
    @PreAuthorize("hasAuthority('HIVECONNECT_TROUBLESHOOTING_READ')")
    public ResponseEntity<List<HiveClient>> getAllHiveclients() {
        try {
            List<HiveClient> hiveclients = hiveclientService.getAllHiveclients();
            return ResponseEntity.ok(hiveclients);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // GET SUBSCRIBER ACCOUNT INFO BY ACCOUNTNUMBER
    // syntax in postman
    // https://hivetest.apolloglobal.net:8081/subscriberAccountInfo?subscriberAccountNumber=dc008

    // EXPOSE THIS API [USED FOR BILLING]
    @Async("asyncExecutor")
    @GetMapping("/subscriberAccountInfo")
    @PreAuthorize("hasAuthority('HIVECONNECT_API_BILLING_ACCESS')")
    public CompletableFuture<ResponseEntity<?>> getSubscriberAccountInfo(
            @RequestParam(required = false) String subscriberAccountNumber, HttpServletRequest request) {
        System.out.println("Authorization header");
        System.out.println(request.getHeader("Authorization"));
        // if (!request.getHeader("Authorization").equals(
        //         "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0YWNjb3VudCIsImlhdCI6MTcyODk4MTA0MH0.6SGymGmjXsK1FgG7tqnirZEYc6r9ZyAvnJP1iEbtdsY")) {
        //     Map<String, Object> errorResponse = new LinkedHashMap<>();
        //     errorResponse.put("path", "/error");
        //     errorResponse.put("error", "Unauthorized");
        //     errorResponse.put("message", "Full authentication is required to access this resource");
        //     errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
        //     return CompletableFuture
        //             .completedFuture(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
        // } else {
        //     System.out.println("Authorized");
        // }
        if (subscriberAccountNumber == null || subscriberAccountNumber.trim().isEmpty()) {
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("timestamp",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
            errorResponse.put("message", "subscriber account number is empty");
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
        }

        try {
            // Fetch subscriber details from the service
            subscriberEntity subscriber = SubscriberService.getSubscriberAccountInfo(subscriberAccountNumber);

            if (subscriber != null) {
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("timestamp",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                response.put("status", HttpStatus.OK.value());
                response.put("message", "subscriber account info");

                Map<String, Object> data = new LinkedHashMap<>();
                data.put("subscriberAccountNumber", subscriber.getSubscriberAccountNumber());
                data.put("packageType", subscriber.getPackageType());
                data.put("fullName", subscriber.getSubscriberName());
                data.put("status", subscriber.getSubsStatus());

                response.put("data", data);

                return CompletableFuture.completedFuture(ResponseEntity.ok(response));
            } else {
                Map<String, Object> errorResponse = new LinkedHashMap<>();
                errorResponse.put("timestamp",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                errorResponse.put("status", HttpStatus.CONFLICT.value());
                errorResponse.put("message", "subscriber account number does not exist");
                return CompletableFuture
                        .completedFuture(ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse));
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("timestamp",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            errorResponse.put("status", HttpStatus.CONFLICT.value());
            errorResponse.put("message", "Error retrieving Subscriber: " + e.getMessage());
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse));
        }
    }

    // GET ALL SUBSCRIBER INFO WITH THIS PARAMETERS
    @Async("asyncExecutor")
    @GetMapping("/getAllsubscribersAccountInfo")
    public CompletableFuture<ResponseEntity<?>> getAllHiveClgetAllSubscriberInfo() {
        try {
            List<HiveClient> hiveClients = hiveclientService.getAllSubscriberInfo(); // Fetch all clients
            if (hiveClients != null && !hiveClients.isEmpty()) {
                List<Map<String, Object>> responseList = hiveClients.stream().map(hiveClient -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("SubscriberAccountNumber", hiveClient.getSubscriberAccountNumber());
                    response.put("subscriberName", hiveClient.getClientName());
                    response.put("packageType", hiveClient.getPackageType());
                    response.put("status", hiveClient.getStatus());
                    return response;
                }).collect(Collectors.toList());

                return CompletableFuture.completedFuture(ResponseEntity.ok(responseList));
            } else {
                return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse(HttpStatus.NOT_FOUND, "No clients/subscribers found")));
            }
        } catch (Exception e) {
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse(HttpStatus.CONFLICT,
                            "Error retrieving clients/subscribers: " + e.getMessage())));
        }
    }

    // GET end point that display Subscriber info & status base on account number
    // provided
    @Async("asyncExecutor")
    @GetMapping("/getsubscriberNetworkInfoby/{accountNumber}")
    public CompletableFuture<ResponseEntity<?>> getHiveClientNetworkInfo(@PathVariable String accountNumber) {
        try {
            HiveClient hiveClient = hiveclientService.getHiveClientByAccountNumber(accountNumber);
            if (hiveClient != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("subscriberAccountNumber", hiveClient.getSubscriberAccountNumber());
                response.put("subscriberName", hiveClient.getClientName());
                response.put("ipAssigned", hiveClient.getIpAssigned());
                response.put("oltIp", hiveClient.getOltIp());
                response.put("packageType", hiveClient.getPackageType());
                response.put("oltReportedUpstream", hiveClient.getOltReportedUpstream());
                response.put("oltReportedDownstream", hiveClient.getOltReportedDownstream());
                return CompletableFuture.completedFuture(ResponseEntity.ok(response));
            } else {
                return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse(HttpStatus.NOT_FOUND,
                                "Subscriber not found with account number: " + accountNumber)));
            }
        } catch (Exception e) {
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse(HttpStatus.CONFLICT, "Error retrieving Subscriber: " + e.getMessage())));
        }
    }

    // ---------------------
    @Async("asyncExecutor")
    @GetMapping("/getsubscribersNetworkInfo")
    public CompletableFuture<ResponseEntity<?>> getAllSubscriberNetworkInfo() {
        try {
            List<HiveClient> hiveClients = hiveclientService.getAllSubscriberNetworkInfo(); // Fetch all subscribers
            if (hiveClients != null && !hiveClients.isEmpty()) {
                List<Map<String, Object>> responseList = hiveClients.stream().map(hiveClient -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("subscriberAccountNumber", hiveClient.getSubscriberAccountNumber());
                    response.put("subscriberName", hiveClient.getClientName());
                    response.put("ipAssigned", hiveClient.getIpAssigned());
                    response.put("oltIp", hiveClient.getOltIp());
                    response.put("packageType", hiveClient.getPackageType());
                    response.put("oltReportedUpstream", hiveClient.getOltReportedUpstream());
                    response.put("oltReportedDownstream", hiveClient.getOltReportedDownstream());
                    return response;
                }).collect(Collectors.toList());

                return CompletableFuture.completedFuture(ResponseEntity.ok(responseList));
            } else {
                return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse(HttpStatus.NOT_FOUND, "No subscribers found")));
            }
        } catch (Exception e) {
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse(HttpStatus.CONFLICT, "Error retrieving subscribers: " + e.getMessage())));
        }
    }

    @Async("asyncExecutor")
    @GetMapping("/getallactiveAccount")
    public CompletableFuture<ResponseEntity<?>> getActiveAndActivatedClients() {
        return hiveclientService.getActiveAndActivatedClients()
                .thenApply(clients -> {
                    if (clients != null && !clients.isEmpty()) {
                        return ResponseEntity.ok(clients);
                    } else {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(createErrorResponse(HttpStatus.NOT_FOUND,
                                        "No clients found with status Active or Activated."));
                    }
                })
                .exceptionally(e -> ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(createErrorResponse(HttpStatus.CONFLICT, "Error retrieving clients: " + e.getMessage())));
    }

}