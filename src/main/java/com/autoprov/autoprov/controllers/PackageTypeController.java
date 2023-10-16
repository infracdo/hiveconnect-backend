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
import com.autoprov.autoprov.repositories.PackageRepository;

@CrossOrigin(origins = "*")
@RestController

public class PackageTypeController {

    private PackageRepository packageRepo;

    @Async("asyncExecutor")
    @GetMapping("/checkPackageBandwidth/{packageTypeId}")
    public CompletableFuture<Optional<Package>> findByPackageTypeId(
            @PathVariable("packageTypeId") String package_type_id) {

        Optional<Package> packageType = packageRepo.findBypackageTypeId(package_type_id);

        return CompletableFuture.completedFuture(packageType);
    }

}
