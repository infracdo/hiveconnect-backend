package com.autoprov.autoprov.controllers;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@CrossOrigin(origins = "*")
@RestController
public class TroubleshootController {

    @Async("asyncExecutor")
    @GetMapping("/getStatus/{device}")
    public String getOnuStatus(@PathVariable("device") String device) {
        String prometheusUrl = "https://dctech-prometheus.apolloglobal.net/api/v1/query?query=lo_status{job=%22ip_address%22,site_tenant=%22DCTECH%22,device_name=\""
                + device + "\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity requestEntity = new HttpEntity("", headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(prometheusUrl, HttpMethod.GET, requestEntity,
                String.class);

        String responseBody = response.getBody();
        System.out.println(responseBody);

        return responseBody;

    }
}
