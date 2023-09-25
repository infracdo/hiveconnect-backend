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
import com.autoprov.autoprov.services.IpAddService;

@RestController
public class IpAddController {

    private IpAddressRepo ipAddRepo;

    @Autowired
    public void IpAddressRepoImpl(IpAddressRepo ipAddRepo) {
        this.ipAddRepo = ipAddRepo;
    }

    // private IpAddService ipAddService;

    // public IpAddController(IpAddService ipAddService) {
    // this.ipAddService = ipAddService;
    // }

    @Async("asyncExecutor")
    @PostMapping("/addOneNetworkAddress")
    public CompletableFuture<String> addOneNetworkAddress(@RequestBody Map<String, String> params) {
        IpAddress ipAdd = IpAddress.builder()
                .networkAddress(params.get("NetworkAddress"))
                .hostAddress("0.0.0." + Integer.parseInt("21"))
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

    }

}