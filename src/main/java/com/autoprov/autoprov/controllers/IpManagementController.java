package com.autoprov.autoprov.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    // @Async("asyncExecutor")
    // @PostMapping("/populateSubnetIPs")
    // public CompletableFuture<String> populateSubnetIPs(@RequestBody Map<String,
    // String> params) {
    // String response =
    // IpListService.populateIpByNetworkAddress(params.get("NetworkAddress"),
    // params.get("InternetGatewayHost"), params.get("OltIpHost"),
    // Integer.parseInt(params.get("VlanID")));

    // return CompletableFuture.completedFuture(response);
    // }

    @Async("asyncExecutor")
    @PostMapping("/addNetworkAddress")
    public CompletableFuture<String> addNetworkAddress(@RequestBody Map<String, String> params) {
        String response = IpListService.addNetworkAddress(params.get("NetworkAddress"), params.get("AccountNumber"),
                params.get("InternetGatewayHost"), params.get("OltIpHost"),
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
    @GetMapping("/getOneAvailableIpAddress")
    public CompletableFuture<List<IpAddress>> getOneAvailableIpAddress() {
        return CompletableFuture.completedFuture(ipAddRepo.getOneAvailableIpAddress());
    }

    @Async("asyncExecutor")
    @GetMapping("/getIpAddressesOfNetworkAddress")
    public CompletableFuture<List<IpAddress>> getIpAddressesOfNetworkAddress(@RequestBody Map<String, String> params) {
        List<IpAddress> NetworkAddress = new ArrayList<>();
        String networkAddress = params.get("NetworkAddress");
        networkAddress = networkAddress.substring(0, (networkAddress.lastIndexOf(".")));
        System.out.println(networkAddress);
        ipAddRepo.findAllUnderNetworkAddress(networkAddress).forEach(NetworkAddress::add);
        return CompletableFuture.completedFuture(NetworkAddress);
    }

    @Async("asyncExecutor")
    @GetMapping("/getIpAddressesOfNetworkAddress/{networkAdd}")
    public CompletableFuture<List<IpAddress>> getIpAddressesOfNetworkAddressPath(
            @PathVariable("networkAdd") String networkAddress) {
        List<IpAddress> NetworkAddress = new ArrayList<>();
        networkAddress = networkAddress.substring(0, (networkAddress.lastIndexOf(".")));
        System.out.println(networkAddress);
        ipAddRepo.findAllUnderNetworkAddress(networkAddress).forEach(NetworkAddress::add);
        return CompletableFuture.completedFuture(NetworkAddress);
    }

    @Async("asyncExecutor")
    @PatchMapping("/updateNetworkAddress/{networkAddress}")
    public CompletableFuture<ResponseEntity<NetworkAddress>> updateNetworkAddress(
            @PathVariable("networkAddress") String networkAddress,
            @RequestBody Map<String, String> params) {
        Optional<NetworkAddress> optionalNetworkAddress = networkdAddRepo.findByNetworkAddress(networkAddress);

        if (optionalNetworkAddress.isPresent()) {
            // Modify the fields of the entity object
            NetworkAddress networkAdd = optionalNetworkAddress.get();

            networkAdd.setAccountNumber(params.get("AccountNumber"));
            networkAdd.setNotes(params.get("Notes"));
            networkAdd.setSite(params.get("Site"));
            networkAdd.setType(params.get("Type"));
            networkAdd.setVlanId(Integer.parseInt(params.get("VlanID")));

            // Save the entity
            return CompletableFuture
                    .completedFuture(new ResponseEntity<>(networkdAddRepo.save(networkAdd), HttpStatus.OK));

        }

        return CompletableFuture.completedFuture(new ResponseEntity<>(HttpStatus.NOT_FOUND));

    }

    @Async("asyncExecutor")
    @PatchMapping("/updateIpAddress/{ipAddress}")
    public CompletableFuture<ResponseEntity<IpAddress>> updateIpAddress(@PathVariable("ipAddress") String ipAddress,
            @RequestBody Map<String, String> params) {
        Optional<IpAddress> optionalIpAddress = ipAddRepo.findByipAddress(ipAddress);

        if (optionalIpAddress.isPresent()) {
            // Modify the fields of the entity object
            IpAddress ipAdd = optionalIpAddress.get();
            if (ipAdd.getAssignable() == false)
                return CompletableFuture.completedFuture(new ResponseEntity<>(HttpStatus.FORBIDDEN));
            ipAdd.setAccountNumber(params.get("AccountNumber"));
            ipAdd.setNotes(params.get("Notes"));
            ipAdd.setStatus(params.get("Status"));

            // Save the entity
            return CompletableFuture
                    .completedFuture(new ResponseEntity<>(ipAddRepo.save(ipAdd), HttpStatus.OK));

        }

        return CompletableFuture.completedFuture(new ResponseEntity<>(HttpStatus.NOT_FOUND));

    }

}