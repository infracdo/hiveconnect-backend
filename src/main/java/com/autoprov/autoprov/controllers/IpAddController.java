package com.autoprov.autoprov.controllers;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.hibernate.query.results.complete.CompleteFetchBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.autoprov.autoprov.domain.IpAddress;
import com.autoprov.autoprov.repositories.IpAddressRepo;
import com.autoprov.autoprov.services.IpListPopulator;

@RestController
public class IpAddController {

    private IpAddressRepo ipAddRepo;

    @Autowired
    IpListPopulator ipListPopulator;

    @Autowired
    public void IpAddressRepoImpl(IpAddressRepo ipAddRepo) {
        this.ipAddRepo = ipAddRepo;
    }

    @Async("asyncExecutor")
    @PostMapping("/addOneNetworkAddress")
    public CompletableFuture<String> addOneNetworkAddress(@RequestBody Map<String, String> params) {
        IpAddress ipAdd = IpAddress.builder()
                .ipAddress(params.get("NetworkAddress"))
                .status("Available")
                .clientId("123")
                .vlanId(5)
                .build();
        ipAddRepo.save(ipAdd);

        return CompletableFuture.completedFuture("Network added");
    }

    @Async("asyncExecutor")
    @PostMapping("/addNetworkAddress")
    public CompletableFuture<String> addNetworkAddress(@RequestBody Map<String, String> params) {
        String response = " ";
        response = IpListPopulator.populateIpByNetworkAddress(params.get("NetworkAddress"));

        return CompletableFuture.completedFuture(response);
    }
}