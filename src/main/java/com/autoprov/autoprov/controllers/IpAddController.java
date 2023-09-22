package com.autoprov.autoprov.controllers;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.autoprov.autoprov.domain.IpAddress;
import com.autoprov.autoprov.repositories.IpAddressRepo;

@RestController
public class IpAddController {
    private IpAddressRepo ipAddRepo;

    @Autowired
    public void IpAddressRepositoryImpl(IpAddressRepo ipAddRepo) {
        this.ipAddRepo = ipAddRepo;
    }

    @Async("asyncExecutor")
    @PostMapping("addNetworkAddress")
    public CompletableFuture<String> addNetworkAddress(@RequestBody Map<String, String> params) {
        ipAddRepo.save(IpAddress.builder().networkAddress(params.get("NetworkAddress"))
                .hostAddress("0.0.0." + Integer.parseInt("21"))
                .status("Available")
                .clientId(null)
                .vlanId(5)
                .build());
        return CompletableFuture.completedFuture("Network Address added");
    }

}