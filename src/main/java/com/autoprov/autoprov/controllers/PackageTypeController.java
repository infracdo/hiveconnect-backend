package com.autoprov.autoprov.controllers;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.autoprov.autoprov.repositories.PackageRepository;
import com.autoprov.autoprov.domain.PackageType;

@CrossOrigin(origins = "*")
@RestController

public class PackageTypeController {

    @Autowired
    private PackageRepository packageRepo;

    @Async("asyncExecutor")
    @GetMapping("/checkPackageBandwidth/{packageTypeId}")
    public CompletableFuture<Optional<PackageType>> findByPackageTypeId(
            @PathVariable("packageTypeId") String package_type_id) {

        return CompletableFuture.completedFuture(packageRepo.findBypackageTypeId(package_type_id));
    }

}
