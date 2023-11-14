package com.autoprov.autoprov.controllers;

import java.net.UnknownHostException;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.autoprov.autoprov.services.IpListService;
import com.autoprov.autoprov.entity.inetDomain.Client;
import com.autoprov.autoprov.entity.ipamDomain.CidrBlock;
import com.autoprov.autoprov.entity.ipamDomain.IpAddress;
import com.autoprov.autoprov.repositories.ipamRepositories.CidrBlockRepository;
import com.autoprov.autoprov.repositories.ipamRepositories.IpAddressRepository;

@CrossOrigin(origins = "*")
@RestController
public class IpManagementController {

    @Autowired
    private IpAddressRepository ipAddRepo;

    @Autowired
    private CidrBlockRepository networkdAddRepo;

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
    @PostMapping("/addCidrBlock")
    public CompletableFuture<String> addCidrBlock(@RequestBody Map<String, String> params) throws UnknownHostException {
        String response = IpListService.addCidrBlock(params.get("CidrBlock"), params.get("AccountNumber"),
                params.get("InternetGatewayHost"), params.get("OltIpHost"),
                (params.get("VlanID")), params.get("Site"), params.get("Type"), params.get("Status"),
                params.get("Notes"));
        return CompletableFuture.completedFuture(response);
    }

    @Async("asyncExecutor")
    @GetMapping("/getCidrBlocks")
    public CompletableFuture<List<CidrBlock>> getNetworkAddresses() {
        List<CidrBlock> CidrBlock = new ArrayList<>();
        networkdAddRepo.findAll().forEach(CidrBlock::add);

        return CompletableFuture.completedFuture(CidrBlock);
    }

    @Async("asyncExecutor")
    @GetMapping("/getAvailableIpAddress")
    public CompletableFuture<List<IpAddress>> getAvailableIpAddress() {
        List<IpAddress> IpAddress = new ArrayList<>();
        ipAddRepo.findAllAvailableIp().forEach(IpAddress::add);
        return CompletableFuture.completedFuture(IpAddress);
    }

    // @Async("asyncExecutor")
    // @GetMapping("/getOneAvailableIpAddress")
    // public CompletableFuture<List<IpAddress>> getOneAvailableIpAddress() {
    // return
    // CompletableFuture.completedFuture(ipAddRepo.getOneAvailableIpAddress());
    // }

    @Async("asyncExecutor")
    @GetMapping("/getOneAvailableIpAddress")
    public CompletableFuture<String> getOneAvailableIpAddress() {
        // TODO: Dynamic Site, get actual IP Address according to Site
        String site = "CDO_1";
        String ipAddress = ipAddRepo
                .getOneAvailableIpAddressUnderSite(site, "Private")
                .get(0)
                .getIpAddress();
        return CompletableFuture.completedFuture(ipAddress);
    }

    @Async("asyncExecutor")
    @GetMapping("/getOneAvailableIpAddress/{cidrBlock}")
    public CompletableFuture<List<IpAddress>> getOneAvailableIpAddressUnderCidrBlock(
            @PathVariable("cidrBlock") String cidrBlock) {
        return CompletableFuture.completedFuture(
                // TODO: add sanitation, input should be in IPv4 format
                ipAddRepo.getOneAvailableIpAddressUnderCidrBlock(cidrBlock.substring(0, cidrBlock.lastIndexOf("."))));
    }

    // @Async("asyncExecutor")
    // @GetMapping("/getIpAddressesOfCidrBlock")
    // public CompletableFuture<List<IpAddress>>
    // getIpAddressesOfCidrBlock(@RequestBody Map<String, String> params) {
    // List<IpAddress> CidrBlock = new ArrayList<>();
    // String cidrBlock = params.get("CidrBlock");
    // cidrBlock = cidrBlock.substring(0, (CidrBlock.lastIndexOf(".")));
    // System.out.println(CidrBlock);
    // ipAddRepo.findAllUnderCidrBlock(cidrBlock).forEach(CidrBlock::add);
    // return CompletableFuture.completedFuture(CidrBlock);
    // }

    @Async("asyncExecutor")
    @GetMapping("/getIpAddressesOfCidrBlock/{cidrBlock}")
    public CompletableFuture<List<IpAddress>> getIpAddressesOfCidrBlockPath(
            @PathVariable("cidrBlock") String cidrBlock) {
        List<IpAddress> CidrBlockIps = new ArrayList<>();
        cidrBlock = cidrBlock.substring(0, (cidrBlock.lastIndexOf(".")));
        System.out.println(cidrBlock);
        ipAddRepo.findAllUnderCidrBlock(cidrBlock).forEach(CidrBlockIps::add);
        return CompletableFuture.completedFuture(CidrBlockIps);
    }

    @Async("asyncExecutor")
    @DeleteMapping("/deleteCidrBlock/{cidrBlock}")
    public CompletableFuture<String> deleteCidrBlock(@PathVariable("cidrBlock") String cidrBlock) {
        ipAddRepo.deleteIpAddressUnderCidrBlock(cidrBlock.substring(0, cidrBlock.lastIndexOf(".")));
        ipAddRepo.deleteCidrBlock(cidrBlock.substring(0, cidrBlock.lastIndexOf(".")));

        return CompletableFuture.completedFuture("IP Address and CIDR Block deleted");
    }

    @Async("asyncExecutor")
    @PatchMapping("/updateNetworkAddress/{networkAddress}")
    public CompletableFuture<ResponseEntity<CidrBlock>> updateNetworkAddress(
            @PathVariable("networkAddress") String networkAddress,
            @RequestBody Map<String, String> params) {
        Optional<CidrBlock> optionalNetworkAddress = networkdAddRepo.findByCidrBlock(networkAddress);

        if (optionalNetworkAddress.isPresent()) {
            // Modify the fields of the entity object
            CidrBlock networkAdd = optionalNetworkAddress.get();

            networkAdd.setAccountNumber(params.get("AccountNumber"));
            networkAdd.setNotes(params.get("Notes"));
            networkAdd.setSite(params.get("Site"));
            networkAdd.setType(params.get("Type"));
            networkAdd.setVlanId(params.get("VlanID"));

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