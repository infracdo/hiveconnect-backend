package com.autoprov.autoprov.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

import com.autoprov.autoprov.entity.oltDomain.oltEntity;
import com.autoprov.autoprov.security.jwt.JwtUtils;
import com.autoprov.autoprov.services.LogService;
import com.autoprov.autoprov.services.oltService;

import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin(origins = "*")
@RestController
public class oltController {

    @Autowired
    private oltService oltService;

    @Autowired
    private LogService logService;
    
    @Async("asyncExecutor")
    @PostMapping("/addnewolt")
    public ResponseEntity<?> createOlt(@RequestBody oltEntity oltEntity, @RequestParam(required = false) String user,
            @RequestParam(required = false) String action, HttpServletRequest request) {
        try {
            oltEntity createdOlt = oltService.createOlt(oltEntity);

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), oltEntity.getOltName() +"/"+ oltEntity.getOltIp(),
                    String.valueOf(HttpStatus.CREATED.value()), request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.status(HttpStatus.CREATED).body(createdOlt);
        } catch (Exception e) {

            logService.logApiError(user, action, request.getMethod(), request.getRequestURI(), oltEntity.getOltName() +"/"+ oltEntity.getOltIp(),
                    String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), e.getMessage(), e.getStackTrace(),
                    request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error"));
        }
    }

    @Async("asyncExecutor")
    @GetMapping("/getOltByName/{oltName}")
    public ResponseEntity<?> getOltByName(@PathVariable String oltName, @RequestParam(required = false) String user,
    @RequestParam(required = false) String action, HttpServletRequest request) {
        Optional<oltEntity> oltEntity = oltService.getOltByName(oltName);
        if (oltEntity.isPresent()) {

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), oltName,
                    String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.status(HttpStatus.OK).body(oltEntity.get());
        } else {

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), oltName,
                    String.valueOf(HttpStatus.NOT_FOUND.value()), request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponse(HttpStatus.NOT_FOUND.value(), "OLT name not found: " + oltName));
        }
    }

    @Async("asyncExecutor")
    @GetMapping("/getOltByIp/{oltIp}")
    public ResponseEntity<?> getOltByIp(@PathVariable String oltIp, @RequestParam(required = false) String user,
    @RequestParam(required = false) String action, HttpServletRequest request) {
        Optional<oltEntity> oltEntity = oltService.getOltByIp(oltIp);
        if (oltEntity.isPresent()) {

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), oltIp,
                    String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.status(HttpStatus.OK).body(oltEntity.get());
        } else {

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), oltIp,
                    String.valueOf(HttpStatus.NOT_FOUND.value()), request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponse(HttpStatus.NOT_FOUND.value(), "OLT IP not found: " + oltIp));
        }
    }
    
    @Async("asyncExecutor")
    @GetMapping("/getallolt")
    // @PreAuthorize("hasAuthority('HIVECONNECT_PROVISIONING_READ')")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<oltEntity>> getAllOlts(@RequestParam(required = false) String user,
    @RequestParam(required = false) String action, HttpServletRequest request) {
        List<oltEntity> olts = oltService.getAllOlts();
        
        logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), null,
                    String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

        return ResponseEntity.status(HttpStatus.OK).body(olts);
    }

    // Error response class to standardize error messages
    static class ErrorResponse {
        private int status;
        private String message;

        public ErrorResponse(int status, String message) {
            this.status = status;
            this.message = message;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
