package com.autoprov.autoprov.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.ReadOnlyProperty;
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

import com.autoprov.autoprov.services.ClientIpService;
import com.autoprov.autoprov.services.IpListService;

import com.autoprov.autoprov.domain.Client;
import com.autoprov.autoprov.domain.IpAddress;

import com.autoprov.autoprov.repositories.ClientRepository;
import com.autoprov.autoprov.repositories.IpAddressRepository;

@CrossOrigin(origins = "*")
@RestController
public class ClientIpController {

    @Autowired
    private ClientRepository clientRepo;

    @Autowired
    private IpAddressRepository ipAddRepo;

    @Async("asyncExecutor")
    @PostMapping("/addNewClient")
    public CompletableFuture<String> addNewClient(@RequestBody Map<String, String> params) {
        String response = ClientIpService.addNewClient(
                params.get("AccountID"),
                params.get("ClientName"),
                params.get("PackageType"),
                params.get("ONUSerialNum"),
                params.get("ONUMacAddress"),
                params.get("OltIP"));

        // ipAddRepo.associateIpAddressToAccountNumber(params.get("ONUSerialNum"),
        // params.get("IPAssigned"));
        return CompletableFuture.completedFuture(response);
    }

    @Async("asyncExecutor")
    @GetMapping("/getClients")

    public CompletableFuture<List<Client>> getClients() {
        List<Client> Client = new ArrayList<>();
        clientRepo.findAll().forEach(Client::add);

        System.out.println("getClients invoked");
        return CompletableFuture.completedFuture(Client);
    }

    @Async("asyncExecutor")
    @GetMapping("/getClientById/{id}")
    public CompletableFuture<Optional<Client>> getClientById(@PathVariable("id") Long id) {

        System.out.println("getClients {" + id.toString() + "} invoked");
        return CompletableFuture.completedFuture(clientRepo.findById(id));
    }

    @Async("asyncExecutor")
    @GetMapping("/getClientBySerialNumber/{serial_number}")
    public CompletableFuture<Optional<Client>> getClientBySerialNumber(
            @PathVariable("serial_number") String serial_number) {

        System.out.println("getClients {" + serial_number + "} invoked");
        return CompletableFuture.completedFuture(clientRepo.findClientBySerialNumber(serial_number));
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
            client.setIp_assigned(params.get("IPAddress"));
            client.setOnu_serial_no(params.get("SerialNumber"));

            // Save the entity
            System.out.println("updateClient {" + id.toString() + "} invoked");

            if (ipAddRepo.findByipAddress(params.get("IPAddress")) == null) {
                return CompletableFuture
                        .completedFuture(new ResponseEntity<>(HttpStatus.FORBIDDEN));
            }

            else {
                ipAddRepo.associateIpAddressToAccountNumber(client.getAccount_No(), params.get("IPAddress"));
                return CompletableFuture
                        .completedFuture(new ResponseEntity<>(clientRepo.save(client), HttpStatus.OK));
            }

        }

        return CompletableFuture.completedFuture(new ResponseEntity<>(HttpStatus.NOT_FOUND));

    }

}
