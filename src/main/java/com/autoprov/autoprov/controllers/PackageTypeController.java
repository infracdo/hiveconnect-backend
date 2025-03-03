package com.autoprov.autoprov.controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationProperties.Http;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.validation.FieldError;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.autoprov.autoprov.entity.subscriberDomain.PackageTypeEntity;
import com.autoprov.autoprov.repositories.subscriberRepositories.PackageRepository;
import com.autoprov.autoprov.security.jwt.JwtUtils;
import com.autoprov.autoprov.services.LogService;
import com.autoprov.autoprov.services.PackageTypeService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@CrossOrigin(origins = "*")
@RestController

public class PackageTypeController {

    @Autowired
    private PackageRepository packageRepo;


     @Autowired
    private PackageTypeService packageTypeService;

    @Autowired
    private LogService logService;

    @Autowired
    private JwtUtils jwtUtils;
    
// POST END POINT add or create new subscriber endpoint
public PackageTypeController(PackageTypeService packageTypeService ){
    this.packageTypeService = packageTypeService;
    
}

@Async("asyncExecutor")
@PostMapping("/createPackage")
public ResponseEntity<?> createPackage(@Valid @RequestBody PackageTypeEntity packageTypeEntity, @RequestParam(required = false) String user,
            @RequestParam(required = false) String action, HttpServletRequest request) {
    try {
        PackageTypeEntity savedPackage = packageTypeService.savePackage(packageTypeEntity);

        logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), packageTypeEntity.getPackageType(),
                    String.valueOf(HttpStatus.CREATED.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

        return ResponseEntity.status(HttpStatus.CREATED).body(createSuccessResponse());
    } catch (SubscriberAlreadyExistsException e) {

        logService.logApiError(user, action, request.getMethod(), request.getRequestURI(), packageTypeEntity.getPackageType(),
                    String.valueOf(HttpStatus.UNAUTHORIZED.value()), e.getMessage(), e.getStackTrace(),
                    request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                             .body(createErrorResponse(HttpStatus.UNAUTHORIZED, "Error saving package. package already exists"));
    } catch (Exception e) {

        logService.logApiError(user, action, request.getMethod(), request.getRequestURI(), packageTypeEntity.getPackageType(),
                    String.valueOf(HttpStatus.CONFLICT.value()), e.getMessage(), e.getStackTrace(),
                    request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));
                    
        return ResponseEntity.status(HttpStatus.CONFLICT)
                             .body(createErrorResponse(HttpStatus.CONFLICT, "Error saving the package: " + e.getMessage()));
    }
}

@ExceptionHandler(MethodArgumentNotValidException.class)
@ResponseStatus(HttpStatus.BAD_REQUEST)
public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
        String fieldName = ((FieldError) error).getField();
        String errorMessage = error.getDefaultMessage();
        errors.put(fieldName, errorMessage);
    });
    
    return errors;
}

private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

private Map<String, Object> createSuccessResponse() {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    Map<String, Object> response = new HashMap<>();
    response.put("timestamp", LocalDateTime.now().format(DATE_TIME_FORMATTER));
    response.put("status", HttpStatus.CREATED.value());
    response.put("message", "New PAckage created successfully");
    return response;
}

private Map<String, Object> createErrorResponse(HttpStatus status, String message) {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("timestamp", LocalDateTime.now().format(DATE_TIME_FORMATTER));
    errorResponse.put("status", status.value());
    errorResponse.put("message", message);
    return errorResponse;
}


//GET ENDPOINT
    @Async("asyncExecutor")
    @GetMapping("/checkPackageDetails/{packageType}")
    public ResponseEntity<Optional<PackageTypeEntity>> findByPackageTypeId(
            @PathVariable("packageType") String package_type, @RequestParam(required = false) String user,
            @RequestParam(required = false) String action, HttpServletRequest request) {

                logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), package_type,
                    String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

        return new ResponseEntity<>(packageRepo.findBypackageId(package_type), HttpStatus.OK);
    }

    @Async("asyncExecutor")
    @GetMapping("/testGetPackageDetails/{packageType}")
    public ResponseEntity<String> testFindByPackageTypeId(
            @PathVariable("packageType") String packageType, @RequestParam(required = false) String user,
            @RequestParam(required = false) String action, HttpServletRequest request) {

        String upstream = "";
        String downstream = "";
        String packageName = "";

        Optional<PackageTypeEntity> optionalPackage = packageRepo.findBypackageId(packageType);
        if (optionalPackage.isPresent()) {

            PackageTypeEntity packageT = optionalPackage.get();
            System.out.println(packageT.toString());
            upstream = convertToKbps(packageT.getUpstream());
            downstream = convertToKbps(packageT.getDownstream());
            packageName = packageT.getPackageType();
        }

        logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), packageType,
                    String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                    jwtUtils.getUserNameFromJwtToken(request.getHeader("Authorization").substring(7)),
                    request.getHeader("User-Agent"));

        return new ResponseEntity<>("Upstream: " + upstream + " Downstream: " + downstream, HttpStatus.OK);
    }

    public static String convertToKbps(String speed) {
        // Pattern pattern = Pattern.compile("(\\d+)(\\s*\\w*)",
        // Pattern.CASE_INSENSITIVE);
        Pattern pattern = Pattern.compile("(\\d+)\\s*([kmgKMG]?)(b?p?s?)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(speed);

        if (matcher.matches()) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2).toLowerCase();
            String suffix = matcher.group(3).toLowerCase();

            switch (suffix) {
                case "kbps":
                    return speed;
                case "mbps":
                    value *= 1000; // Convert to kbps
                    break;
                case "gbps":
                    value *= 1000000; // Convert to kbps
                    break;
                case "bps":
                    // Do nothing, already in bps
                    break;
                case "":
                    // If no suffix, assume kbps
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported unit: " + suffix);
            }

            return value + " kbps";
        } else {
            throw new IllegalArgumentException("Invalid speed format: " + speed);
        }
    }

}