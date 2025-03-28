package com.autoprov.autoprov.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.autoprov.autoprov.entity.subscriberDomain.PackageTypeEntity;
import com.autoprov.autoprov.repositories.subscriberRepositories.PackageRepository;
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

    // POST END POINT add or create new subscriber endpoint
    public PackageTypeController(PackageTypeService packageTypeService) {
        this.packageTypeService = packageTypeService;

    }

    @Async("asyncExecutor")
    @PostMapping("/createPackage")
    public ResponseEntity<?> createPackage(@Valid @RequestBody PackageTypeEntity packageTypeEntity,
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String action, HttpServletRequest request) {
        try {
            packageTypeService.savePackage(packageTypeEntity);

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                    packageTypeEntity.getPackageType() + "/" + packageTypeEntity.getUpstream() + "/"
                            + packageTypeEntity.getDownstream(),
                    String.valueOf(HttpStatus.CREATED.value()), request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(logService.createResponse(HttpStatus.CREATED, "Package has been created successfully"));

        } catch (SubscriberAlreadyExistsException e) {

            logService.logApiError(user, action, request.getMethod(), request.getRequestURI(),
                    packageTypeEntity.getPackageType() + "/" + packageTypeEntity.getUpstream() + "/"
                            + packageTypeEntity.getDownstream(),
                    String.valueOf(HttpStatus.CONFLICT.value()), e.getMessage(), e.getStackTrace(),
                    request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(logService.createResponse(HttpStatus.CONFLICT, "Package already exists"));
        } catch (Exception e) {

            logService.logApiError(user, action, request.getMethod(), request.getRequestURI(),
                    packageTypeEntity.getPackageType() + "/" + packageTypeEntity.getUpstream() + "/"
                            + packageTypeEntity.getDownstream(),
                    String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), e.getMessage(), e.getStackTrace(),
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
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return errors;
    }

    @Async("asyncExecutor")
    @GetMapping("/checkPackageDetails/{packageType}")
    public ResponseEntity<?> findByPackageTypeId(
            @PathVariable("packageType") String package_type, @RequestParam(required = false) String user,
            @RequestParam(required = false) String action, HttpServletRequest request) {
        try {
            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), package_type,
                    String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.ok(packageRepo.findBypackageId(package_type));
        } catch (Exception e) {

            logService.logApiError(user, action, request.getMethod(), request.getRequestURI(), package_type,
                    String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), e.getMessage(),
                    e.getStackTrace(),
                    request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    logService.createResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                            "An error occurred. " + e.getMessage()));
        }

    }

    @Async("asyncExecutor")
    @GetMapping("/testGetPackageDetails/{packageType}")
    public ResponseEntity<?> testFindByPackageTypeId(
            @PathVariable("packageType") String packageType, @RequestParam(required = false) String user,
            @RequestParam(required = false) String action, HttpServletRequest request) {
        String upstream = "";
        String downstream = "";
        String packageName = "";
        try {
            Optional<PackageTypeEntity> optionalPackage = packageRepo.findBypackageId(packageType);
        if (optionalPackage.isPresent()) {

            PackageTypeEntity packageT = optionalPackage.get();
            System.out.println("package details " + packageT.toString());
            upstream = convertToKbps(packageT.getUpstream());
            downstream = convertToKbps(packageT.getDownstream());
            packageName = packageT.getPackageType();
        }

        logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), packageType,
                String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                request.getHeader("Authorization"),
                request.getHeader("User-Agent"));

        return ResponseEntity.ok("Upstream: " + upstream + " Downstream: " + downstream);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    logService.createResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred. " + e.getMessage()));
        }
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