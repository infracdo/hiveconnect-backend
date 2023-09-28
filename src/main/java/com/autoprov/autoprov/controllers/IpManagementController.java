package com.autoprov.autoprov.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.CrossOrigin;
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

@CrossOrigin(origins = "*")
@RestController
public class IpManagementController {

    @Autowired
    private IpAddressRepository ipAddRepo;

    @Autowired
    private NetworkAddressRepository networkdAddRepo;

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

    @Async("asyncExecutor")
    @GetMapping("/getIpAddressesOfNetworkAddress")
    public CompletableFuture<List<IpAddress>> getIpAddressesOfNetworkAddress(@RequestBody Map<String, String> params) {
        List<IpAddress> IpAddress = new ArrayList<>();
        String networkAddress = params.get("NetworkAddress");
        networkAddress = networkAddress.substring(0, (networkAddress.lastIndexOf(".")));
        ipAddRepo.findAllUnderNetworkAddress(networkAddress).forEach(IpAddress::add);
        return CompletableFuture.completedFuture(IpAddress);
    }

}