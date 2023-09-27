package com.autoprov.autoprov.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.autoprov.autoprov.domain.Client;
import com.autoprov.autoprov.services.IpListService;

import com.autoprov.autoprov.domain.IpAddress;
import com.autoprov.autoprov.domain.NetworkAddress;
import com.autoprov.autoprov.repositories.IpAddressRepository;
import com.autoprov.autoprov.repositories.NetworkAddressRepository;

@RestController
public class IpManagementController {

    @Autowired
    private IpAddressRepository ipAddRepo;

    @Autowired
    private NetworkAddressRepository networkdAddRepo;

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
        String response = IpListService.populateIpByNetworkAddress(params.get("NetworkAddress"),
                Integer.parseInt(params.get("VlanID")));

        return CompletableFuture.completedFuture(response);
    }

    @Async("asyncExecutor")
    @PostMapping("/addNetworkAddress")
    public CompletableFuture<String> addNetworkAddress(@RequestBody Map<String, String> params) {
        String response = IpListService.addNetworkAddress(params.get("NetworkAddress"), params.get("AccountNumber"),
                Integer.parseInt(params.get("VlanID")), params.get("Site"), params.get("Type"), params.get("Status"),
                params.get("Notes"));
        return CompletableFuture.completedFuture(response);
    }

    @Async("asyncExecutor")
    @GetMapping("/getNetworkAddresses")
    public CompletableFuture<List<NetworkAddress>> getNetworkAddresses() {
        List<NetworkAddress> NetworkAddress = new ArrayList<>();
        networkdAddRepo.findAll().forEach(NetworkAddress::add);

        return CompletableFuture.completedFuture(NetworkAddress);
    }

    @Async("asyncExecutor")
    @GetMapping("/getAvailableIpAddress")
    public CompletableFuture<List<IpAddress>> getAvailableIpAddress() {
        List<IpAddress> IpAddress = new ArrayList<>();
        ipAddRepo.findAllAvailableIp().forEach(IpAddress::add);
        return CompletableFuture.completedFuture(IpAddress);
    }

}