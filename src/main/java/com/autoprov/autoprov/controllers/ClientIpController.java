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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.autoprov.autoprov.services.IpListService;

import com.autoprov.autoprov.domain.Client;

import com.autoprov.autoprov.repositories.ClientRepository;

@RestController
public class ClientIpController {

    @Autowired
    private ClientRepository clientRepo;

    @Async("asyncExecutor")
    @PostMapping("/associateIpToClient")
    public CompletableFuture<String> associateIpToClient(@RequestBody Map<String, String> params) {
        return null;
    }

    @Async("asyncExecutor")
    @GetMapping("/getClients")
    public CompletableFuture<List<Client>> getClients() {
        List<Client> Client = new ArrayList<>();
        clientRepo.findAll().forEach(Client::add);

        return CompletableFuture.completedFuture(Client);
    }

    @Async("asyncExecutor")
    @GetMapping("/getClientById/{id}")
    public CompletableFuture<Optional<Client>> getClientById(@PathVariable("id") Long id) {

        return CompletableFuture.completedFuture(clientRepo.findById(id));
    }

    @Async("asyncExecutor")
    @PatchMapping("/updateClient/{id}")
    public CompletableFuture<ResponseEntity<Client>> updateClient(@PathVariable("id") Long id,
            @RequestBody Map<String, String> params) {
        // Retrieve the entity object
        Optional<Client> optionalClient = clientRepo.findById(id);

        if (optionalClient.isPresent()) {
            // Modify the fields of the entity object
            Client client = optionalClient.get();
            client.setIp_assigned(params.get("IP Address"));
            client.setOnu_serial_no(params.get("Serial Number"));

            // Save the entity
            return CompletableFuture
                    .completedFuture(new ResponseEntity<>(clientRepo.save(client), HttpStatus.OK));

        }

        return CompletableFuture.completedFuture(new ResponseEntity<>(HttpStatus.NOT_FOUND));

    }

}
