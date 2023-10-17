package com.autoprov.autoprov.controllers;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.autoprov.autoprov.repositories.OltRepository;
import com.autoprov.autoprov.repositories.PackageRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.autoprov.autoprov.domain.Client;
import com.autoprov.autoprov.domain.Olt;
import com.autoprov.autoprov.domain.PackageType;

@CrossOrigin(origins = "*")
@RestController

public class OltController {

    @Autowired
    private OltRepository oltRepo;

    @Async("asyncExecutor")
    @GetMapping("/checkOltIpBySiteName/{oltSite}")
    public CompletableFuture<Optional<Olt>> findByOltSite(
            @PathVariable("oltSite") String oltSite) {

        return CompletableFuture.completedFuture(oltRepo.findByOlt_site(oltSite));
    }

    @Async("asyncExecutor")
    @GetMapping("/checkOltSiteByIp/{oltIp}")
    public CompletableFuture<Optional<Olt>> findByOltIp(
            @PathVariable("oltIp") String oltIp) {

        return CompletableFuture.completedFuture(oltRepo.findByOlt_ip(oltIp));
    }

    @Async("asyncExecutor")
    @GetMapping("/checkOltInterface/{device_name}")
    public String checkOltInterface(@PathVariable("device_name") String deviceName) {

        String ansibleApiUrl = "https://dctech-netbox.apolloglobal.net/api/dcim/devices/?name=" + deviceName;
        String accessToken = "f5e09a0d4702c1eff0d7b04a0a100d65e2175703";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Token " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        // Make the HTTP GET request
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                ansibleApiUrl,
                HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(headers),
                String.class);

        // Process the response
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String responseBody = responseEntity.getBody();
            System.out.println(responseBody);

            try {
                // Use ObjectMapper to extract a specific node
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                String onuInterface = " ";

                JsonNode configContextNode = jsonNode.path("results").get(0).path("config_context");

                for (JsonNode element : configContextNode) {
                    onuInterface = element.fieldNames().next();
                    System.out.println("Key Name inside config_context: " + onuInterface);
                }
                // int onuIndex = nodeValue.indexOf("interface onu", 0);
                // String onuInterface = nodeValue.substring(onuIndex + 13, onuIndex + 19);
                // System.out.println("Value of the node: " + onuInterface);
                return onuInterface;
            } catch (Exception e) {
                // Handle the exception
                e.printStackTrace();
                return null;
            }
        } else {
            // Handle the error
            System.err.println("Error: " + responseEntity.getStatusCode());
            return null;
        }
    }

}
