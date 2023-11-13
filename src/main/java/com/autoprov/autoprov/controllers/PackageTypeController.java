package com.autoprov.autoprov.controllers;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.autoprov.autoprov.entity.inetDomain.PackageType;
import com.autoprov.autoprov.repositories.inetRepositories.PackageRepository;

@CrossOrigin(origins = "*")
@RestController

public class PackageTypeController {

    @Autowired
    private PackageRepository packageRepo;

    @Async("asyncExecutor")
    @GetMapping("/checkPackageDetails/{packageTypeId}")
    public CompletableFuture<Optional<PackageType>> findByPackageTypeId(
            @PathVariable("packageTypeId") String package_type_id) {

        return CompletableFuture.completedFuture(packageRepo.findBypackageId(package_type_id));
    }

}
