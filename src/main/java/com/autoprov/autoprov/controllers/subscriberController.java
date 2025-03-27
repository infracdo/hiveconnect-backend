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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import com.autoprov.autoprov.dto.AbsTokenResponse;
import com.autoprov.autoprov.entity.hiveDomain.HiveClient;
import com.autoprov.autoprov.entity.subscriberDomain.subscriberEntity;
import com.autoprov.autoprov.repositories.hiveRepositories.HiveClientRepository;
import com.autoprov.autoprov.repositories.hiveRepositories.VlanInfoRepository;
import com.autoprov.autoprov.repositories.subscriberRepositories.subscriberRepository;
import com.autoprov.autoprov.security.jwt.JwtUtils;
import com.autoprov.autoprov.services.AbsService;
import com.autoprov.autoprov.services.DhcpService;
import com.autoprov.autoprov.services.HiveClientService;
import com.autoprov.autoprov.services.LogService;
import com.autoprov.autoprov.services.subscriberService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@CrossOrigin(origins = "*")
@RestController
public class subscriberController {

    @Value("${absApiUrl}")
    private String absApiUrl;

    @Value("${absTokenUrl}")
    private String absTokenUrl;

    @Value("${absApiKey}")
    private String absApiKey;

    @Value("${absTokenUsername}")
    private String absTokenUsername;

    @Value("${absTokenPassword}")
    private String absTokenPassword;

    @Autowired
    private AbsService absService;

    @Autowired
    private final subscriberService SubscriberService;

    @Autowired
    private final HiveClientService hiveclientService;

    @Autowired
    private VlanInfoRepository vlanInfoRepository;

    @Autowired
    private HiveClientRepository hiveClientRepository;

    @Autowired
    private subscriberRepository subscriberRepository;

    @Autowired
    private LogService logService;

    // POST END POINT add or create new subscriber endpoint
    public subscriberController(subscriberService SubscriberService, HiveClientService hiveclientService) {
        this.SubscriberService = SubscriberService;
        this.hiveclientService = hiveclientService;
    }

    // EXPOSE THIS API [USED FOR BILLING]
    @Async("asyncExecutor")
    @PostMapping("/createSubscriberForProvisioning")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> addSubscriberForProvisioning(@Valid @RequestBody subscriberEntity subscriberEntity,
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String action, HttpServletRequest request) {
        try {
            if (subscriberEntity.getSubscriberAccountNumber() == null
                    || subscriberEntity.getSubscriberAccountNumber().trim().isEmpty()) {

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                        subscriberEntity.getSubscriberAccountNumber() + "/"
                                + subscriberEntity.getSubscriberName() + "/"
                                + subscriberEntity.getPackageType() + "/"
                                + subscriberEntity.getProvision() + "/"
                                + subscriberEntity.getSubsStatus(),
                        String.valueOf(HttpStatus.BAD_REQUEST.value()), request.getRemoteAddr(),
                        request.getHeader("Authorization"),
                        request.getHeader("User-Agent"));

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(logService.createResponse(HttpStatus.BAD_REQUEST,
                                "Account number is missing/invalid"));
            }

            if (subscriberEntity.getSubscriberName() == null
                    || subscriberEntity.getSubscriberName().trim().isEmpty()) {

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                        subscriberEntity.getSubscriberAccountNumber() + "/"
                                + subscriberEntity.getSubscriberName() + "/"
                                + subscriberEntity.getPackageType() + "/"
                                + subscriberEntity.getProvision() + "/"
                                + subscriberEntity.getSubsStatus(),
                        String.valueOf(HttpStatus.BAD_REQUEST.value()), request.getRemoteAddr(),
                        request.getHeader("Authorization"),
                        request.getHeader("User-Agent"));

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(logService.createResponse(HttpStatus.BAD_REQUEST,
                                "Name is missing/invalid"));
            }

            if (subscriberEntity.getProvision() == null
                    || subscriberEntity.getProvision().trim().isEmpty()) {

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                        subscriberEntity.getSubscriberAccountNumber() + "/"
                                + subscriberEntity.getSubscriberName() + "/"
                                + subscriberEntity.getPackageType() + "/"
                                + subscriberEntity.getProvision() + "/"
                                + subscriberEntity.getSubsStatus(),
                        String.valueOf(HttpStatus.BAD_REQUEST.value()), request.getRemoteAddr(),
                        request.getHeader("Authorization"),
                        request.getHeader("User-Agent"));

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(logService.createResponse(HttpStatus.BAD_REQUEST,
                                "Provision is missing/invalid"));
            }

            if (subscriberEntity.getPackageType() == null
                    || subscriberEntity.getPackageType().trim().isEmpty()) {

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                        subscriberEntity.getSubscriberAccountNumber() + "/"
                                + subscriberEntity.getSubscriberName() + "/"
                                + subscriberEntity.getPackageType() + "/"
                                + subscriberEntity.getProvision() + "/"
                                + subscriberEntity.getSubsStatus(),
                        String.valueOf(HttpStatus.BAD_REQUEST.value()), request.getRemoteAddr(),
                        request.getHeader("Authorization"),
                        request.getHeader("User-Agent"));

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(logService.createResponse(HttpStatus.BAD_REQUEST,
                                "Package type is missing/invalid"));
            }

            Optional<HiveClient> clientOptional = hiveClientRepository
                    .findBySubscriberAccountNumber(subscriberEntity.getSubscriberAccountNumber());
            if (clientOptional.isPresent()) {

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                        subscriberEntity.getSubscriberAccountNumber() + "/"
                                + subscriberEntity.getSubscriberName() + "/"
                                + subscriberEntity.getPackageType() + "/"
                                + subscriberEntity.getProvision() + "/"
                                + subscriberEntity.getSubsStatus(),
                        String.valueOf(HttpStatus.CONFLICT.value()), request.getRemoteAddr(),
                        request.getHeader("Authorization"),
                        request.getHeader("User-Agent"));

                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(logService.createResponse(HttpStatus.CONFLICT,
                                "Subscriber already exists"));
            }

            // Set status to NEW
            subscriberEntity.setSubsStatus("NEW");

            subscriberEntity savedSubscriber = SubscriberService.saveSubscriber(subscriberEntity);

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                    subscriberEntity.getSubscriberAccountNumber() + "/"
                            + subscriberEntity.getSubscriberName() + "/"
                            + subscriberEntity.getPackageType() + "/"
                            + subscriberEntity.getProvision() + "/"
                            + subscriberEntity.getSubsStatus(),
                    String.valueOf(HttpStatus.CREATED.value()), request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(logService.createResponse(HttpStatus.CREATED,
                            "Subscriber has been created successfully"));
        } catch (SubscriberAlreadyExistsException e) {

            logService.logApiError(user, action, request.getMethod(), request.getRequestURI(),
                    subscriberEntity.getSubscriberAccountNumber() + "/"
                            + subscriberEntity.getSubscriberName() + "/"
                            + subscriberEntity.getPackageType() + "/"
                            + subscriberEntity.getProvision() + "/"
                            + subscriberEntity.getSubsStatus(),
                    String.valueOf(HttpStatus.CONFLICT.value()), e.getMessage(), e.getStackTrace(),
                    request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(logService.createResponse(HttpStatus.CONFLICT,
                            e.getMessage()));
        } catch (Exception e) {

            logService.logApiError(user, action, request.getMethod(), request.getRequestURI(),
                    subscriberEntity.getSubscriberAccountNumber() + "/"
                            + subscriberEntity.getSubscriberName() + "/"
                            + subscriberEntity.getPackageType() + "/"
                            + subscriberEntity.getProvision() + "/"
                            + subscriberEntity.getSubsStatus(),
                    String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), e.getMessage(),
                    e.getStackTrace(),
                    request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(logService.createResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                            "An error occurred. " + e.getMessage()));
        }
    }

    // EXPOSE THIS API [USED FOR MIGRATION]
    @Async("asyncExecutor")
    @PostMapping("/createSubscriberForMigration")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> addSubscriberForMigration(@Valid @RequestBody HiveClient hiveClient,
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String action, HttpServletRequest request) {
        try {
            // Check if the account number is empty
            if (hiveClient.getSubscriberAccountNumber() == null
                    || hiveClient.getSubscriberAccountNumber().trim().isEmpty()) {

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                        hiveClient.getSubscriberAccountNumber() + "/"
                                + hiveClient.getClientName() + "/"
                                + hiveClient.getOnuDeviceName() + "/"
                                + hiveClient.getPackageType() + "/"
                                + hiveClient.getProvision() + "/"
                                + hiveClient.getStatus(),
                        String.valueOf(HttpStatus.BAD_REQUEST.value()), request.getRemoteAddr(),
                        request.getHeader("Authorization"),
                        request.getHeader("User-Agent"));

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(logService.createResponse(HttpStatus.BAD_REQUEST,
                                "Account number is missing/invalid"));
            }

            Optional<subscriberEntity> clientOptional = subscriberRepository
                    .findBySubscriberAccountNumber(hiveClient.getSubscriberAccountNumber());
            if (clientOptional.isPresent()) {

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                        hiveClient.getSubscriberAccountNumber() + "/"
                                + hiveClient.getClientName() + "/"
                                + hiveClient.getOnuDeviceName() + "/"
                                + hiveClient.getPackageType() + "/"
                                + hiveClient.getProvision() + "/"
                                + hiveClient.getStatus(),
                        String.valueOf(HttpStatus.CONFLICT.value()), request.getRemoteAddr(),
                        request.getHeader("Authorization"),
                        request.getHeader("User-Agent"));

                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(logService.createResponse(HttpStatus.CONFLICT,
                                "Subscriber already exists"));
            }

            if (hiveClient.getProvision() == null || hiveClient.getProvision().trim().isEmpty()
                    || hiveClient.getProvision().length() > 50) {

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                        hiveClient.getSubscriberAccountNumber() + "/"
                                + hiveClient.getClientName() + "/"
                                + hiveClient.getOnuDeviceName() + "/"
                                + hiveClient.getPackageType() + "/"
                                + hiveClient.getProvision() + "/"
                                + hiveClient.getStatus(),
                        String.valueOf(HttpStatus.BAD_REQUEST.value()), request.getRemoteAddr(),
                        request.getHeader("Authorization"),
                        request.getHeader("User-Agent"));

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(logService.createResponse(HttpStatus.BAD_REQUEST,
                                "Provision is missing/invalid"));
            }

            // Check if the subscriber name is empty or too long
            if (hiveClient.getClientName() == null || hiveClient.getClientName().trim().isEmpty()) {

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                        hiveClient.getSubscriberAccountNumber() + "/"
                                + hiveClient.getClientName() + "/"
                                + hiveClient.getOnuDeviceName() + "/"
                                + hiveClient.getPackageType() + "/"
                                + hiveClient.getProvision() + "/"
                                + hiveClient.getStatus(),
                        String.valueOf(HttpStatus.BAD_REQUEST.value()), request.getRemoteAddr(),
                        request.getHeader("Authorization"),
                        request.getHeader("User-Agent"));

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(logService.createResponse(HttpStatus.BAD_REQUEST,
                                "Client name is missing/invalid"));
            }

            if (hiveClient.getOnuDeviceName() == null || hiveClient.getOnuDeviceName().trim().isEmpty()) {

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                        hiveClient.getSubscriberAccountNumber() + "/"
                                + hiveClient.getClientName() + "/"
                                + hiveClient.getOnuDeviceName() + "/"
                                + hiveClient.getPackageType() + "/"
                                + hiveClient.getProvision() + "/"
                                + hiveClient.getStatus(),
                        String.valueOf(HttpStatus.BAD_REQUEST.value()), request.getRemoteAddr(),
                        request.getHeader("Authorization"),
                        request.getHeader("User-Agent"));

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(logService.createResponse(HttpStatus.BAD_REQUEST,
                                "ONU name is missing/invalid"));
            }

            if (hiveClient.getPackageType() == null || hiveClient.getPackageType().trim().isEmpty()
                    || hiveClient.getPackageType().length() > 50) {

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                        hiveClient.getSubscriberAccountNumber() + "/"
                                + hiveClient.getClientName() + "/"
                                + hiveClient.getOnuDeviceName() + "/"
                                + hiveClient.getPackageType() + "/"
                                + hiveClient.getProvision() + "/"
                                + hiveClient.getStatus(),
                        String.valueOf(HttpStatus.BAD_REQUEST.value()), request.getRemoteAddr(),
                        request.getHeader("Authorization"),
                        request.getHeader("User-Agent"));

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(logService.createResponse(HttpStatus.BAD_REQUEST,
                                "Package type is missing/invalid"));
            }

            if (hiveClient.getStatus() == null || hiveClient.getStatus().trim().isEmpty()
                    || hiveClient.getStatus().length() > 50
                    || !(hiveClient.getStatus().trim().equalsIgnoreCase("onhold")
                            || hiveClient.getStatus().trim().equalsIgnoreCase("active"))) {

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                        hiveClient.getSubscriberAccountNumber() + "/"
                                + hiveClient.getClientName() + "/"
                                + hiveClient.getOnuDeviceName() + "/"
                                + hiveClient.getPackageType() + "/"
                                + hiveClient.getProvision() + "/"
                                + hiveClient.getStatus(),
                        String.valueOf(HttpStatus.BAD_REQUEST.value()), request.getRemoteAddr(),
                        request.getHeader("Authorization"),
                        request.getHeader("User-Agent"));

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(logService.createResponse(HttpStatus.BAD_REQUEST,
                                "Status is missing/invalid"));
            } else {
                hiveClient.setStatus(hiveClient.getStatus().toUpperCase() + "_PENDING_MIGRATION");
            }

            HiveClientService.addHiveMigratedClient(hiveClient.getSubscriberAccountNumber(),
                    hiveClient.getClientName(),
                    hiveClient.getOnuSerialNumber(), hiveClient.getOnuDeviceName(),
                    hiveClient.getOnuMacAddress(),
                    hiveClient.getStatus(), hiveClient.getOltIp(), hiveClient.getOltInterface(),
                    hiveClient.getIpAssigned(),
                    hiveClient.getProvision(), hiveClient.getSsidName(),
                    hiveClient.getPackageType(), hiveClient.getOltReportedUpstream(),
                    hiveClient.getOltReportedDownstream());

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                    hiveClient.getSubscriberAccountNumber() + "/" + hiveClient.getClientName() + "/"
                            + hiveClient.getOnuDeviceName() + "/"
                            + hiveClient.getPackageType() + "/" + hiveClient.getProvision()
                            + "/" + hiveClient.getStatus(),
                    String.valueOf(HttpStatus.CREATED.value()), request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(logService.createResponse(HttpStatus.CREATED,
                            "Subscriber has been created successfully"));
        } catch (SubscriberAlreadyExistsException e) {

            logService.logApiError(user, action, request.getMethod(), request.getRequestURI(),
                    hiveClient.getSubscriberAccountNumber() + "/" + hiveClient.getClientName() + "/"
                            + hiveClient.getOnuDeviceName() + "/"
                            + hiveClient.getPackageType() + "/" + hiveClient.getProvision()
                            + "/" + hiveClient.getStatus(),
                    String.valueOf(HttpStatus.CONFLICT.value()), e.getMessage(), e.getStackTrace(),
                    request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(logService.createResponse(HttpStatus.CONFLICT,
                            "Subscriber already exists"));
        } catch (Exception e) {

            logService.logApiError(user, action, request.getMethod(), request.getRequestURI(),
                    hiveClient.getSubscriberAccountNumber() + "/" + hiveClient.getClientName() + "/"
                            + hiveClient.getOnuDeviceName() + "/"
                            + hiveClient.getPackageType() + "/" + hiveClient.getProvision()
                            + "/" + hiveClient.getStatus(),
                    String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), e.getMessage(),
                    e.getStackTrace(),
                    request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(logService.createResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                            "An error occurred. " + e.getMessage()));
        }
    }

    // EXPOSE THIS API [USED FOR MIGRATION]
    @Async("asyncExecutor")
    @PostMapping("/updateMigrationSubscriberStatus")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateMigratedSubscriberStatus(@RequestBody Map<String, String> params,
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String action, HttpServletRequest request) {
        String subscriberAccountNumber = params.get("subscriberAccountNumber");

        // Check if the subscriber account number is empty or null
        if (subscriberAccountNumber == null
                || subscriberAccountNumber.trim().isEmpty()) {

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                    subscriberAccountNumber,
                    String.valueOf(HttpStatus.BAD_REQUEST.value()), request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(logService.createResponse(HttpStatus.BAD_REQUEST,
                            "Account number is missing/invalid"));
        }

        Optional<HiveClient> clientOptional = hiveClientRepository
                .findBySubscriberAccountNumber(subscriberAccountNumber);
        if (!clientOptional.isPresent()) {

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                    subscriberAccountNumber,
                    String.valueOf(HttpStatus.NOT_FOUND.value()), request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(logService.createResponse(HttpStatus.NOT_FOUND,
                            "Subscriber does not exist"));
        }

        try {
            HiveClient client = clientOptional.get();

            if (client.getStatus() == null || client.getStatus().trim().isEmpty()
                    || client.getStatus().length() > 50
                    || !client.getStatus().contains("_PENDING_MIGRATION")) {

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                        subscriberAccountNumber,
                        String.valueOf(HttpStatus.BAD_REQUEST.value()), request.getRemoteAddr(),
                        request.getHeader("Authorization"),
                        request.getHeader("User-Agent"));

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(logService.createResponse(HttpStatus.BAD_REQUEST,
                                "Subscriber is not for migration"));
            }

            String newStatus = client.getStatus().replace("_PENDING_MIGRATION", "");
            client.setStatus(newStatus);

            ResponseEntity<?> absResponse = absService.statusCallBack(newStatus,
                    subscriberAccountNumber);

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                    subscriberAccountNumber,
                    String.valueOf(absResponse.getStatusCode().value()), request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            if (absResponse.getStatusCode().equals(HttpStatus.OK)) {
                hiveClientRepository.save(client);
            }

            return ResponseEntity.status(absResponse.getStatusCode()).body(absResponse.getBody());
        } catch (HttpStatusCodeException e) {

            logService.logApiError(user, action, request.getMethod(), request.getRequestURI(),
                    subscriberAccountNumber,
                    String.valueOf(e.getStatusCode().value()), e.getResponseBodyAsString(),
                    e.getStackTrace(),
                    request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.status(e.getStatusCode())
                    .body(logService.createResponse(HttpStatus.valueOf(e.getStatusCode().value()),
                            e.getResponseBodyAsString()));
        } catch (Exception e) {

            logService.logApiError(user, action, request.getMethod(), request.getRequestURI(),
                    subscriberAccountNumber,
                    String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), e.getMessage(),
                    e.getStackTrace(),
                    request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(logService.createResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                            "An error occurred. " + e.getMessage()));
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldError().getDefaultMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(logService.createResponse(HttpStatus.BAD_REQUEST, errorMessage));
    }

    // GET endpoint to retrieve all subscribers
    // USED IN FRONTEND TO GET FOR PROVISIONED SUBSCRIBERS
    @Async("asyncExecutor")
    @GetMapping("/getsubscribers")
    // @PreAuthorize("hasAuthority('HIVECONNECT_PROVISIONING_READ')")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getAllSubscribers(@RequestParam(required = false) String user,
            @RequestParam(required = false) String action, HttpServletRequest request) {
        try {
            List<subscriberEntity> subscribers = SubscriberService.getAllSubscribers();

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), null,
                    String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.ok(subscribers);
        } catch (Exception e) {

            logService.logApiError(user, action, request.getMethod(), request.getRequestURI(), null,
                    String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), e.getMessage(),
                    e.getStackTrace(),
                    request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(logService.createResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                            "An error occurred. " + e.getMessage()));
        }
    }

    // DEBUGGING
    @Async("asyncExecutor")
    @GetMapping("/getprovisionedsubscribers")
    // @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getProvisionedSubscribers(@RequestParam(required = false) String user,
            @RequestParam(required = false) String action, HttpServletRequest request) {
        try {
            List<HiveClient> provisionedSubscribers = hiveclientService.getActiveOnholdSubscribers();

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), null,
                    String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.ok(provisionedSubscribers);
        } catch (Exception e) {

            logService.logApiError(user, action, request.getMethod(), request.getRequestURI(), null,
                    String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), e.getMessage(),
                    e.getStackTrace(),
                    request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(logService.createResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                            "An error occurred. " + e.getMessage()));
        }
    }

    // EXPOSE THIS API [USED FOR MIGRATION]
    @Async("asyncExecutor")
    @GetMapping("/getmigratingsubscribers")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getMigratingSubscribers(@RequestParam(required = false) String user,
            @RequestParam(required = false) String action, HttpServletRequest request) {
        try {
            List<HiveClient> migratingSubscribers = hiveclientService.getAllMigratingSubscribers();

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), null,
                    String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.ok(migratingSubscribers);
        } catch (Exception e) {

            logService.logApiError(user, action, request.getMethod(), request.getRequestURI(), null,
                    String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), e.getMessage(),
                    e.getStackTrace(),
                    request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(logService.createResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                            "An error occurred. " + e.getMessage()));
        }
    }

    // GET endpoint to retrieve subscriber by id
    // USED IN FRONTEND TO GET SUBSCRIBERS FOR PROVISIONING
    @Async("asyncExecutor")
    @GetMapping("/getsubscriberbyid/{id}")
    public ResponseEntity<?> getSubscriberById(@PathVariable Long id, @RequestParam(required = false) String user,
            @RequestParam(required = false) String action, HttpServletRequest request) {
        try {
            subscriberEntity subscriber = SubscriberService.getSubscriberById(id);
            if (subscriber != null) {

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                        id.toString(),
                        String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                        request.getHeader("Authorization"),
                        request.getHeader("User-Agent"));

                return ResponseEntity.ok(subscriber);
            } else {

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                        id.toString(),
                        String.valueOf(HttpStatus.NOT_FOUND.value()), request.getRemoteAddr(),
                        request.getHeader("Authorization"),
                        request.getHeader("User-Agent"));

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(logService.createResponse(HttpStatus.NOT_FOUND,
                                "Subscriber does not exist"));
            }
        } catch (Exception e) {

            logService.logApiError(user, action, request.getMethod(), request.getRequestURI(),
                    id.toString(),
                    String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), e.getMessage(),
                    e.getStackTrace(),
                    request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(logService.createResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                            "An error occurred. " + e.getMessage()));
        }

    }

    // DEBUGGING
    // GET endpoint to retrieve subscriber by id
    @Async("asyncExecutor")
    @GetMapping("/getHiveClientById/{id}")
    public ResponseEntity<?> getHiveClientById(@PathVariable Long id, @RequestParam(required = false) String user,
            @RequestParam(required = false) String action, HttpServletRequest request) {
        try {
            HiveClient hive = hiveclientService.getHiveClientById(id);
            if (hive != null) {

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                        id.toString(),
                        String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                        request.getHeader("Authorization"),
                        request.getHeader("User-Agent"));

                return ResponseEntity.ok(hive);
            } else {

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                        id.toString(),
                        String.valueOf(HttpStatus.NOT_FOUND.value()), request.getRemoteAddr(),
                        request.getHeader("Authorization"),
                        request.getHeader("User-Agent"));

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(logService.createResponse(HttpStatus.NOT_FOUND,
                                "Client not found"));
            }
        } catch (Exception e) {

            logService.logApiError(user, action, request.getMethod(), request.getRequestURI(),
                    id.toString(),
                    String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), e.getMessage(),
                    e.getStackTrace(),
                    request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(logService.createResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                            "An error occurred. " + e.getMessage()));
        }
    }

    // GET endpoint to retrieve all hiveclients
    @Async("asyncExecutor")
    @GetMapping("/getHiveClients")
    // @PreAuthorize("hasAuthority('HIVECONNECT_TROUBLESHOOTING_READ')")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getAllHiveclients(@RequestParam(required = false) String user,
            @RequestParam(required = false) String action, HttpServletRequest request) {
        try {
            List<HiveClient> hiveclients = hiveclientService.getAllHiveclients();

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), null,
                    String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.ok(hiveclients);
        } catch (Exception e) {

            logService.logApiError(user, action, request.getMethod(), request.getRequestURI(), null,
                    String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), e.getMessage(),
                    e.getStackTrace(),
                    request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    logService.createResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred. " + e.getMessage()));
        }
    }

    // GET SUBSCRIBER ACCOUNT INFO BY ACCOUNTNUMBER
    // syntax in postman
    // https://hivetest.apolloglobal.net:8081/subscriberAccountInfo?subscriberAccountNumber=dc008

    // EXPOSE THIS API [USED FOR BILLING]
    @Async("asyncExecutor")
    @GetMapping("/subscriberAccountInfo")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getSubscriberAccountInfo(
            @RequestParam(required = false) String subscriberAccountNumber,
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String action, HttpServletRequest request) {
        Map<String, Object> response = new LinkedHashMap<>();

        // if (!request.getHeader("Authorization").equals(
        // "Bearer
        // eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0YWNjb3VudCIsImlhdCI6MTcyODk4MTA0MH0.6SGymGmjXsK1FgG7tqnirZEYc6r9ZyAvnJP1iEbtdsY"))
        // {
        // Map<String, Object> errorResponse = new LinkedHashMap<>();
        // errorResponse.put("path", "/error");
        // errorResponse.put("error", "Unauthorized");
        // errorResponse.put("message", "Full authentication is required to access this
        // resource");
        // errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
        // return CompletableFuture
        // .completedFuture(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
        // } else {
        // System.out.println("Authorized");
        // }
        if (subscriberAccountNumber == null || subscriberAccountNumber.trim().isEmpty()) {

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                    subscriberAccountNumber,
                    String.valueOf(HttpStatus.BAD_REQUEST.value()), request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(logService.createResponse(HttpStatus.BAD_REQUEST, "Account number is missing/invalid"));
        }

        try {
            // Fetch subscriber details from the service
            HiveClient subscriber = hiveclientService.getClientByAccountNumber(subscriberAccountNumber);

            if (subscriber != null) {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("subscriberAccountNumber", subscriber.getSubscriberAccountNumber());
                data.put("packageType", subscriber.getPackageType());
                data.put("fullName", subscriber.getClientName());
                data.put("status", subscriber.getStatus());

                response.put("timestamp",
                        LocalDateTime.now().format(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                response.put("status", HttpStatus.OK.value());
                response.put("message", "Subscriber account info");
                response.put("data", data);

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                        subscriberAccountNumber,
                        String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                        request.getHeader("Authorization"),
                        request.getHeader("User-Agent"));

                return ResponseEntity.ok(response);
            } else {
                subscriberEntity new_subscriber = SubscriberService
                        .getSubscriberAccountInfo(subscriberAccountNumber);

                if (new_subscriber != null) {
                    Map<String, Object> data = new LinkedHashMap<>();
                    data.put("subscriberAccountNumber", new_subscriber.getSubscriberAccountNumber());
                    data.put("packageType", new_subscriber.getPackageType());
                    data.put("fullName", new_subscriber.getSubscriberName());
                    data.put("status", new_subscriber.getSubsStatus());

                    response.put("timestamp",
                            LocalDateTime.now().format(
                                    DateTimeFormatter.ofPattern(
                                            "yyyy-MM-dd HH:mm:ss")));
                    response.put("status", HttpStatus.OK.value());
                    response.put("message", "Subscriber account info");
                    response.put("data", data);

                    logService.logApiAccess(user, action, request.getMethod(),
                            request.getRequestURI(),
                            subscriberAccountNumber,
                            String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                            request.getHeader("Authorization"),
                            request.getHeader("User-Agent"));

                    return ResponseEntity.ok(response);
                } else {

                    logService.logApiAccess(user, action, request.getMethod(),
                            request.getRequestURI(),
                            subscriberAccountNumber,
                            String.valueOf(HttpStatus.NOT_FOUND.value()),
                            request.getRemoteAddr(),
                            request.getHeader("Authorization"),
                            request.getHeader("User-Agent"));

                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(logService.createResponse(HttpStatus.NOT_FOUND, "Subscriber does not exist"));
                }
            }
        } catch (Exception e) {

            logService.logApiError(user, action, request.getMethod(), request.getRequestURI(),
                    subscriberAccountNumber,
                    String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), e.getMessage(),
                    e.getStackTrace(),
                    request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    logService.createResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred. " + e.getMessage()));
        }
    }

    // RETURN ACCOUNT INFO OF ALL SUBSCRIBERS
    @Async("asyncExecutor")
    @GetMapping("/getAllsubscribersAccountInfo")
    public ResponseEntity<?> getAllHiveClgetAllSubscriberInfo(@RequestParam(required = false) String user,
            @RequestParam(required = false) String action, HttpServletRequest request) {
        try {
            List<HiveClient> hiveClients = hiveclientService.getAllSubscriberInfo(); // Fetch all clients
            if (hiveClients != null && !hiveClients.isEmpty()) {
                List<Map<String, Object>> responseList = hiveClients.stream().map(hiveClient -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("SubscriberAccountNumber",
                            hiveClient.getSubscriberAccountNumber());
                    response.put("subscriberName", hiveClient.getClientName());
                    response.put("packageType", hiveClient.getPackageType());
                    response.put("status", hiveClient.getStatus());
                    return response;
                }).collect(Collectors.toList());

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                        null,
                        String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                        request.getHeader("Authorization"),
                        request.getHeader("User-Agent"));

                return ResponseEntity.ok(responseList);
            } else {

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                        null,
                        String.valueOf(HttpStatus.NOT_FOUND.value()), request.getRemoteAddr(),
                        request.getHeader("Authorization"),
                        request.getHeader("User-Agent"));

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(logService.createResponse(HttpStatus.NOT_FOUND,
                                "No subscribers found"));
            }
        } catch (Exception e) {

            logService.logApiError(user, action, request.getMethod(), request.getRequestURI(), null,
                    String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), e.getMessage(),
                    e.getStackTrace(),
                    request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(logService.createResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                            "An error occurred. " + e.getMessage()));
        }
    }

    // RETURN SUBSCRIBER COUNT OF SPECIFIC STATUS AND LOCATION
    @Async("asyncExecutor")
    @GetMapping("/getStatusCount")
    public ResponseEntity<?> getSubscriberStatusCount(@RequestParam(required = false) String status,
            @RequestParam(required = false) String location, @RequestParam(required = false) String user,
            @RequestParam(required = false) String action, HttpServletRequest request) {
        if (status == null || status.trim().isEmpty()) {

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                    status, String.valueOf(HttpStatus.BAD_REQUEST.value()), request.getRemoteAddr(),
                    request.getHeader("Authorization"), request.getHeader("User-Agent"));

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(logService.createResponse(HttpStatus.BAD_REQUEST, "Status is missing/invalid"));
        }

        try {
            if (location == null || location.trim().isEmpty()) {
                Long clientCount = hiveclientService.getStatusCount(status); // Fetch all clients

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                        status,
                        String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                        request.getHeader("Authorization"),
                        request.getHeader("User-Agent"));

                return ResponseEntity.ok(clientCount);
            }

            List<String> subscribers = vlanInfoRepository.getSubscriberAccountNoByLocation(location);
            Integer count = 0;
            for (String accountNo : subscribers) {
                // Use the findBySubscriberAccountNumber method to check if the subscriber
                // exists
                Optional<HiveClient> hiveClientOptional = hiveClientRepository.findClientByAccountNoStatus(accountNo,
                        status);

                if (hiveClientOptional.isPresent()) {
                    count++;
                }
            }
            return ResponseEntity.ok(count);
        } catch (Exception e) {

            logService.logApiError(user, action, request.getMethod(), request.getRequestURI(), null,
                    String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), e.getMessage(),
                    e.getStackTrace(),
                    request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    logService.createResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred. " + e.getMessage()));
        }
    }

    // GET end point that display Subscriber info & status base on account number
    // provided
    @Async("asyncExecutor")
    @GetMapping("/getsubscriberNetworkInfoby/{accountNumber}")
    public ResponseEntity<?> getHiveClientNetworkInfo(@PathVariable String accountNumber,
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String action, HttpServletRequest request) {
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

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                        accountNumber,
                        String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                        request.getHeader("Authorization"),
                        request.getHeader("User-Agent"));

                return ResponseEntity.ok(response);
            } else {

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                        accountNumber,
                        String.valueOf(HttpStatus.NOT_FOUND.value()), request.getRemoteAddr(),
                        request.getHeader("Authorization"),
                        request.getHeader("User-Agent"));

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(logService.createResponse(HttpStatus.NOT_FOUND,
                                "Client not found"));
            }
        } catch (Exception e) {

            logService.logApiError(user, action, request.getMethod(), request.getRequestURI(),
                    accountNumber,
                    String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), e.getMessage(),
                    e.getStackTrace(),
                    request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(logService.createResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                            "An error occurred. " + e.getMessage()));
        }
    }

    // ---------------------
    @Async("asyncExecutor")
    @GetMapping("/getsubscribersNetworkInfo")
    public ResponseEntity<?> getAllSubscriberNetworkInfo(@RequestParam(required = false) String user,
            @RequestParam(required = false) String action, HttpServletRequest request) {
        try {
            List<HiveClient> hiveClients = hiveclientService.getAllSubscriberNetworkInfo();
            if (hiveClients != null && !hiveClients.isEmpty()) {
                List<Map<String, Object>> responseList = hiveClients.stream().map(hiveClient -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("subscriberAccountNumber",
                            hiveClient.getSubscriberAccountNumber());
                    response.put("subscriberName", hiveClient.getClientName());
                    response.put("ipAssigned", hiveClient.getIpAssigned());
                    response.put("oltIp", hiveClient.getOltIp());
                    response.put("packageType", hiveClient.getPackageType());
                    response.put("oltReportedUpstream", hiveClient.getOltReportedUpstream());
                    response.put("oltReportedDownstream", hiveClient.getOltReportedDownstream());
                    return response;
                }).collect(Collectors.toList());

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                        null,
                        String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                        request.getHeader("Authorization"),
                        request.getHeader("User-Agent"));

                return ResponseEntity.ok(responseList);
            } else {

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                        null,
                        String.valueOf(HttpStatus.NOT_FOUND.value()), request.getRemoteAddr(),
                        request.getHeader("Authorization"),
                        request.getHeader("User-Agent"));

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(logService.createResponse(HttpStatus.NOT_FOUND,
                                "No subscriber found"));
            }
        } catch (Exception e) {

            logService.logApiError(user, action, request.getMethod(), request.getRequestURI(), null,
                    String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), e.getMessage(),
                    e.getStackTrace(),
                    request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(logService.createResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                            "An error occurred. " + e.getMessage()));
        }
    }

    @Async("asyncExecutor")
    @GetMapping("/getallactiveAccount")
    public ResponseEntity<?> getActiveAndActivatedClients(@RequestParam(required = false) String user,
            @RequestParam(required = false) String action, HttpServletRequest request) {
        try {
            List<Map<String, Object>> clients = hiveclientService.getActiveAndActivatedClients();
            if (hiveclientService.getActiveAndActivatedClients() != null && !clients.isEmpty()) {

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                        null,
                        String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                        request.getHeader("Authorization"),
                        request.getHeader("User-Agent"));

                return ResponseEntity.ok(clients);
            } else {

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                        null,
                        String.valueOf(HttpStatus.NOT_FOUND.value()), request.getRemoteAddr(),
                        request.getHeader("Authorization"),
                        request.getHeader("User-Agent"));

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(logService.createResponse(HttpStatus.NOT_FOUND,
                                "No subscriber with Active or Activated status found"));
            }
        } catch (Exception e) {

            logService.logApiError(user, action, request.getMethod(), request.getRequestURI(), null,
                    String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), e.getMessage(),
                    e.getStackTrace(),
                    request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(logService.createResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                            "An error occurred. " + e.getMessage()));
        }
    }
}