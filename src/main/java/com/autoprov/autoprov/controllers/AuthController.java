package com.autoprov.autoprov.controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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

import jakarta.servlet.http.HttpServletRequest;
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

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role.toLowerCase()) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);

                        break;
                    case "mod":
                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);

                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PostMapping("/logaction")
    public ResponseEntity<?> logUserAction(HttpServletRequest request, @RequestBody Map<String, String> params) {
        JwtUtils jwtutils = new JwtUtils();
        String user = "N/A"; // admin - sent by frontend, empty if not via frontend
        String ip = params.get("client_ip"); // 127.0.0.1 - sent by endpoint that invoked this method
        String action = "API REQUEST TO BACKEND API"; // accessed hiveconnect rogue devices - sent by frontend, default value if not via frontend
        String endpoint = params.get("accessed_endpoint"); // /test - sent by endpoint that invoked this method
        String payload = params.get("payload"); // RES-123-456 - sent by endpoint that invoked this method
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String response_status = params.get("response_status"); // 200 OK - sent by endpoint that invoked this method
        String token_info = jwtutils.getUserNameFromJwtToken(params.get("token_info")); // used static token of frontendaccount - sent by endpoint that invoked this method
        String user_agent = request.getHeader("User-Agent"); // sent by endpoint that invoked this method

        if (params.containsKey("user") && params.get("user") != null) {
            user = params.get("user");
        }

        if (params.containsKey("action") && params.get("action") != null) {
            action = params.get("action");
        }

        String responseMessage = String.format("[%s]: User %s %s %s from %s. Additional info: %s.", timestamp, user, action, endpoint, user_agent, token_info);
        System.out.println("responseMessage: " + responseMessage);
       try {
            ApiResponse response = new ApiResponse(HttpStatus.CREATED.value(), responseMessage);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            ApiResponse response = new ApiResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
}