package com.autoprov.autoprov.controllers;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.autoprov.autoprov.services.IpListService;

@RestController
public class IpManagementController {

    // private IpAddressRepository ipAddRepo;

    // @Autowired
    // IpListService ipListPopulator;

    // @Autowired
    // public void IpAddressRepoImpl(IpAddressRepository ipAddRepo) {
    // this.ipAddRepo = ipAddRepo;
    // }

    // // @Async("asyncExecutor")
    // // @PostMapping("/addOneNetworkAddress")
    // // public CompletableFuture<String> addOneNetworkAddress(@RequestBody
    // Map<String, String> params) {
    // // IpAddress ipAdd = IpAddress.builder()
    // // .ipAddress(params.get("NetworkAddress"))
    // // .status("Available")
    // // .clientId("123")
    // // .vlanId(5)
    // // .build();
    // // ipAddRepo.save(ipAdd);

    // // return CompletableFuture.completedFuture("Network added");
    // // }

    @Async("asyncExecutor")
    @PostMapping("/populateSubnetIPs")
    public CompletableFuture<String> populateSubnetIPs(@RequestBody Map<String, String> params) {
        String response = IpListService.populateIpByNetworkAddress(params.get("NetworkAddress"));

        return CompletableFuture.completedFuture(response);
    }

    @Async("asyncExecutor")
    @PostMapping("/addNetworkAddress")
    public CompletableFuture<String> addNetworkAddress(@RequestBody Map<String, String> params) {
        String response = " ";
        return CompletableFuture.completedFuture(response);
    }

}