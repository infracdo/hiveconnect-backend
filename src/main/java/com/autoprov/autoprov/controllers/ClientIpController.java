package com.autoprov.autoprov.controllers;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.autoprov.autoprov.services.IpListService;

@RestController
public class ClientIpController {

    @Async("asyncExecutor")
    @PostMapping("/associateIpToClient")
    public CompletableFuture<String> associateIpToClient(@RequestBody Map<String, String> params) {
        return null;
    }

}
