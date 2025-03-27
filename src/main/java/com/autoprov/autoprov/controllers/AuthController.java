package com.autoprov.autoprov.controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.autoprov.autoprov.dto.ApiResponse;
import com.autoprov.autoprov.entity.subscriberDomain.models.ERole;
import com.autoprov.autoprov.entity.subscriberDomain.models.Role;
import com.autoprov.autoprov.entity.subscriberDomain.models.User;
import com.autoprov.autoprov.payload.request.LoginRequest;
import com.autoprov.autoprov.payload.request.SignupRequest;
import com.autoprov.autoprov.payload.response.JwtResponse;
import com.autoprov.autoprov.payload.response.MessageResponse;
import com.autoprov.autoprov.repositories.subscriberRepositories.RoleRepository;
import com.autoprov.autoprov.repositories.subscriberRepositories.UserRepository;
import com.autoprov.autoprov.security.jwt.JwtUtils;
import com.autoprov.autoprov.security.services.UserDetailsImpl;
import com.autoprov.autoprov.services.LogService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    private LogService logService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest,
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String action, HttpServletRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(), loginRequest.getUsername(),
                String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                request.getHeader("Authorization"),
                request.getHeader("User-Agent"));

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest,
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String action, HttpServletRequest request) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                    signUpRequest.getUsername() + "/" + signUpRequest.getEmail() + "/" + signUpRequest.getRole(),
                    String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(logService.createResponse(HttpStatus.BAD_REQUEST,
                            "Username is already taken"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {

            logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                    signUpRequest.getUsername() + "/" + signUpRequest.getEmail() + "/" + signUpRequest.getRole(),
                    String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                    request.getHeader("Authorization"),
                    request.getHeader("User-Agent"));

                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(logService.createResponse(HttpStatus.BAD_REQUEST,
                            "Email is already in use"));
        }

        // Create new user's account
        User new_user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Role is not found"));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role.toLowerCase()) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Role is not found"));
                        roles.add(adminRole);

                        break;
                    case "mod":
                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Role is not found"));
                        roles.add(modRole);

                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Role is not found"));
                        roles.add(userRole);
                }
            });
        }

        new_user.setRoles(roles);
        userRepository.save(new_user);

        logService.logApiAccess(user, action, request.getMethod(), request.getRequestURI(),
                signUpRequest.getUsername() + "/" + signUpRequest.getEmail() + "/" + signUpRequest.getRole(),
                String.valueOf(HttpStatus.OK.value()), request.getRemoteAddr(),
                request.getHeader("Authorization"),
                request.getHeader("User-Agent"));

        return ResponseEntity.status(HttpStatus.CREATED)
                    .body(logService.createResponse(HttpStatus.CREATED,
                            "User registered successfully"));
    }
}