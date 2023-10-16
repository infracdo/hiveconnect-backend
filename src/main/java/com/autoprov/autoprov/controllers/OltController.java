package com.autoprov.autoprov.controllers;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.autoprov.autoprov.repositories.OltRepository;
import com.autoprov.autoprov.repositories.PackageRepository;
import com.autoprov.autoprov.domain.Olt;
import com.autoprov.autoprov.domain.PackageType;

@CrossOrigin(origins = "*")
@RestController

public class OltController {

    @Autowired
    private OltRepository oltRepo;

    @Async("asyncExecutor")
    @GetMapping("/checkOltIpBySiteName/{oltSite}")
    public CompletableFuture<Optional<Olt>> findByOltSite(
            @PathVariable("oltSite") String oltSite) {

        return CompletableFuture.completedFuture(oltRepo.findByOlt_site(oltSite));
    }

    @Async("asyncExecutor")
    @GetMapping("/checkOltSiteByIp/{oltIp}")
    public CompletableFuture<Optional<Olt>> findByOltIp(
            @PathVariable("oltIp") String oltIp) {

        return CompletableFuture.completedFuture(oltRepo.findByOlt_ip(oltIp));
    }

}
