package com.autoprov.autoprov.security;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.CrossOrigin;

import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import com.autoprov.autoprov.security.jwt.AuthEntryPointJwt;
import com.autoprov.autoprov.security.jwt.AuthTokenFilter;
import com.autoprov.autoprov.security.services.UserDetailsServiceImpl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@SuppressWarnings("deprecation")
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
// (securedEnabled = true,
// jsr250Enabled = true,
// prePostEnabled = true) // by default
public class WebSecurityConfig { // extends WebSecurityConfigurerAdapter {

  String jwkSetUri = "https://wcdssi.apolloglobal.net:8443/auth/realms/workconnect-test/protocol/openid-connect/certs";

  @Autowired
  UserDetailsServiceImpl userDetailsService;

  @Autowired
  private AuthEntryPointJwt unauthorizedHandler;

  @Bean
  public AuthTokenFilter authenticationJwtTokenFilter() {
    return new AuthTokenFilter();
  }

  // @Override
  // public void configure(AuthenticationManagerBuilder
  // authenticationManagerBuilder) throws Exception {
  // authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
  // }

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() { // Custom converter to map JWT roles to Spring
                                                                   // Security authorities
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

    // Set up the converter to map JWT roles into Spring Security authorities
    converter.setJwtGrantedAuthoritiesConverter(jwt -> {
      // Extract the "realm_access" claim as a Map
      Map<String, Object> realmAccess = jwt.getClaim("realm_access");

      // Extract the "resource_access" claim as a Map
      Map<String, Object> resourceAccess = jwt.getClaim("resource_access");

      // Safely extract the roles list from the "realm_access" map
      List<String> roles = Collections.emptyList();

      if (realmAccess != null) {
        // Get the "roles" from "realm_access" and safely cast it
        Object rolesObj = realmAccess.get("roles");

        // If rolesObj is an instance of List, cast safely
        if (rolesObj instanceof List<?>) {
          // Check if the List contains String elements
          roles = ((List<?>) rolesObj).stream()
              .filter(item -> item instanceof String)
              .map(item -> (String) item)
              .collect(Collectors.toList());
        }
      }

      // Process roles from "resource_access"
      if (resourceAccess != null) {
        Object clientAccessObj = resourceAccess.get("test-hiveconnect-client");
        if (clientAccessObj instanceof Map<?, ?>) {
          // Safe check for Map type before casting
          @SuppressWarnings("unchecked") // Suppress the unchecked cast warning here
          Map<String, Object> clientAccess = (Map<String, Object>) clientAccessObj;

          if (clientAccess.get("roles") instanceof List) {
            List<?> clientRoles = (List<?>) clientAccess.get("roles");
            // Add the roles from resource_access (test-workconnect-client)
            roles.addAll(clientRoles.stream()
                .filter(role -> role instanceof String)
                .map(role -> (String) role)
                .collect(Collectors.toList()));
          }
        }
      }

      // Map roles to authorities (SimpleGrantedAuthority)
      return roles.stream()
          .map(role -> new SimpleGrantedAuthority(role)) // Add 'ROLE_' prefix if needed
          .collect(Collectors.toList());
    });

    return converter;
  }

  @Bean
  JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withJwkSetUri(this.jwkSetUri).build();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());

    return authProvider;
  }

  // @Bean
  // @Override
  // public AuthenticationManager authenticationManagerBean() throws Exception {
  // return super.authenticationManagerBean();
  // }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
    return authConfig.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // @Override
  // protected void configure(HttpSecurity http) throws Exception {
  // http.cors().and().csrf().disable()
  // .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
  // .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
  // .authorizeRequests().antMatchers("/api/auth/**").permitAll()
  // .antMatchers("/api/test/**").permitAll()
  // .anyRequest().authenticated();
  //
  // http.addFilterBefore(authenticationJwtTokenFilter(),
  // UsernamePasswordAuthenticationFilter.class);
  // }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    http.csrf(csrf -> csrf.disable())
    .exceptionHandling(exception ->
    exception.authenticationEntryPoint(unauthorizedHandler))
    .sessionManagement(session ->
    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    .authorizeHttpRequests((authorize) -> authorize
    // .requestMatchers("/api/test/**").permitAll()
    // .requestMatchers("/createSubscriberForProvisioning").permitAll()
    // .requestMatchers("/subscriberAccountInfo").permitAll()
    // .requestMatchers("/updateSubscriberProvision").permitAll()
    // .requestMatchers("/activateSubscriber").permitAll()
    // .requestMatchers("/deactivateSubscriber").permitAll()
    // .requestMatchers("/terminateSubscriber").permitAll()
    .anyRequest().permitAll())
    // .oauth2ResourceServer((oauth2) -> oauth2
    // .jwt(withDefaults()))
    ;
    return http.build();

    // http.csrf(csrf -> csrf.disable())
    // .exceptionHandling(exception ->
    // exception.authenticationEntryPoint(unauthorizedHandler))
    // .sessionManagement(session ->
    // session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    // .authorizeHttpRequests(auth ->
    // auth.requestMatchers("/api/auth/**").permitAll()
    // .requestMatchers("/executeProvision").permitAll()
    // .requestMatchers("/executeAutoConfig").permitAll()
    // .requestMatchers("/preprovisionCheck").permitAll()
    // .requestMatchers("/executeMonitoring").permitAll()
    // .requestMatchers("/lastJobStatus").permitAll()
    // .requestMatchers("/getOltInterface").permitAll()
    // .requestMatchers("/getOltBandwidth").permitAll()
    // .requestMatchers("/simulateHiveMonitoringError").permitAll()
    // .requestMatchers("/getOltInterface/{jobId}").permitAll()
    // .requestMatchers("/testExecuteMonitoring").permitAll()
    // .requestMatchers("/getRogueDevices").permitAll()
    // .requestMatchers("/addnetwork").permitAll()
    // .requestMatchers("/getallnetworks").permitAll()
    // .requestMatchers("/cidripaddresses").permitAll()
    // .requestMatchers("/addnewolt").permitAll()
    // .requestMatchers("/getOltByName/{oltName}").permitAll()
    // .requestMatchers("/getOltByIp/{oltIp}").permitAll()
    // .requestMatchers("/getallolt").permitAll()
    // .requestMatchers("/createPackage").permitAll()
    // .requestMatchers("/checkPackageDetails/{packageType}").permitAll()
    // .requestMatchers("/testGetPackageDetails/{packageType}").permitAll()
    // .requestMatchers("/executeInetAutoProv").permitAll()
    // .requestMatchers("/executeInetMonitoring").permitAll()
    // .requestMatchers("/getsubscribers").permitAll()
    // .requestMatchers("/getsubscriberbyid/{id}").permitAll()
    // .requestMatchers("/getHiveClientById/{id}").permitAll()
    // .requestMatchers("/getHiveClients").permitAll()
    // .requestMatchers("/getAllsubscribersAccountInfo").permitAll()
    // .requestMatchers("/getsubscriberNetworkInfoby/{accountNumber}").permitAll()
    // .requestMatchers("/getsubscribersNetworkInfo").permitAll()
    // .requestMatchers("/getallactiveAccount").permitAll()
    // .requestMatchers("/getprovisionedsubscribers").permitAll()
    // .requestMatchers("/subscriberAccountInfo").permitAll()
    // .requestMatchers("/getIpAddressesOfCidrBlock/{cidrBlock}").permitAll()
    // .requestMatchers("/getClientBySerialNumber/{serial_number}").permitAll()
    // .requestMatchers("/api/auth/signup").permitAll()
    // .anyRequest().authenticated()
    // );

    // http.authenticationProvider(authenticationProvider());

    // http.addFilterBefore(authenticationJwtTokenFilter(),
    // UsernamePasswordAuthenticationFilter.class);

    
    // // First part: General configuration (csrf, exception handling, session management)
    // http.csrf(csrf -> csrf.disable())
    //     .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
    //     .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    // // Second part: Authorization configuration
    // http
    //     .authorizeHttpRequests((authorize) -> authorize
    //         // Public endpoints with no authentication
    //         .requestMatchers("/api/test/**", "/api/auth/**").permitAll() // No authentication for these endpoints
    //         // Specific paths requiring custom authentication
    //         .requestMatchers("/createSubscriberForProvisioning", "/updateSubscriberProvision",
    //             "/updateSubscriberPackage", "/subscriberAccountInfo", "/activateSubscriber", "/deactivateSubscriber",
    //             "/terminateSubscriber")
    //         .authenticated()  // Ensure authentication is required for these specific paths
    //         .anyRequest().authenticated())  // Apply OAuth2 authentication for all other requests

    //     // Apply OAuth2 Resource Server (JWT) to all paths except the ones specified above
    //     .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()));  // Apply JWT validation for all requests by default

    // // Apply custom authentication provider and JWT filter for the specific endpoints only
    // http
    //     .authorizeHttpRequests((authorize) -> authorize
    //         .requestMatchers("/createSubscriberForProvisioning", "/updateSubscriberProvision",
    //             "/updateSubscriberPackage", "/subscriberAccountInfo", "/activateSubscriber", "/deactivateSubscriber",
    //             "/terminateSubscriber")
    //         .authenticated()) // Ensure authentication is required for these paths
    //     .authenticationProvider(authenticationProvider()) // Apply custom authentication provider
    //     .addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);  // Apply custom JWT filter

    // return http.build();
  }
}
