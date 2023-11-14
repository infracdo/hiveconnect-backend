package com.autoprov.autoprov.controllers;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @Async("asyncExecutor")
    @GetMapping("/testGetPackageDetails/{packageTypeId}")
    public CompletableFuture<String> testFindByPackageTypeId(
            @PathVariable("packageTypeId") String packageId) {

        String upstream = "";
        String downstream = "";
        String packageName = "";

        Optional<PackageType> optionalPackage = packageRepo.findBypackageId(packageId);
        if (optionalPackage.isPresent()) {

            PackageType packageT = optionalPackage.get();
            System.out.println(packageT.toString());
            upstream = convertToKbps(packageT.getUpstream());
            downstream = convertToKbps(packageT.getDownstream());
            packageName = packageT.getName();

        }

        return CompletableFuture.completedFuture("Upstream: " + upstream + " Downstream: " + downstream);
    }

    public static String convertToKbps(String speed) {
        Pattern pattern = Pattern.compile("(\\d+)(\\s*\\w*)");
        Matcher matcher = pattern.matcher(speed);

        if (matcher.matches()) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2).trim().toLowerCase();

            switch (unit) {
                case "kbps":
                    return speed;
                case "mbps":
                    value *= 1000; // Convert to kbps
                    return value + " kbps";
                case "":
                    return value + " kbps"; // Assume default is kbps
                default:
                    throw new IllegalArgumentException("Unsupported unit: " + unit);
            }
        } else {
            throw new IllegalArgumentException("Invalid speed format: " + speed);
        }
    }

}
