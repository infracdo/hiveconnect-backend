package com.autoprov.autoprov.services;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.autoprov.autoprov.entity.hiveDomain.HiveClient;
import com.autoprov.autoprov.repositories.hiveRepositories.HiveClientRepository;

@Service
public class HiveClientService {

    private static HiveClientRepository hiveClientRepo;

    @Autowired
    public void HiveClientRepoImpl(HiveClientRepository hiveClientRepo) {
        HiveClientService.hiveClientRepo = hiveClientRepo;
    }

    public static String addHiveNewClient(String accountNo, String clientName, String serialNumber, String deviceName,
            String macAddress,
            String oltIp,
            String oltInterface, String ipAddress, String ssidName, String packageType, String upstream,
            String downstream) {

        HiveClient newHiveClient = HiveClient.builder()
                .subscriberAccountNumber(accountNo)
                .clientName(clientName)
                .onuSerialNumber(serialNumber)
                .onuDeviceName(deviceName)
                .onuMacAddress(macAddress).status("ACTIVE")
                .oltIp(oltIp)
                .oltInterface(oltInterface)
                .ipAssigned(ipAddress)
                .provision("HiveConnect")
                .ssidName(ssidName)
                .packageType(packageType)
                .oltReportedUpstream(upstream)
                .oltReportedDownstream(downstream)
                .build();
        hiveClientRepo.save(newHiveClient);

        return "Successful";
    }


     public List<HiveClient> getAllHiveclients() {
        return hiveClientRepo.findAll();
    }


    public HiveClient getHiveClientByAccountNumber(String subscriberAccountNumber) {
        return hiveClientRepo.findBySubscriberAccountNumber(subscriberAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Subscriber not found with account number: " + subscriberAccountNumber));
    }

    public HiveClient getHiveClientNetworkInfo(String subscriberAccountNumber) {
        return hiveClientRepo.findBySubscriberAccountNumber(subscriberAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Subscriber not found with account number: " + subscriberAccountNumber));
    }

    @Async("asyncExecutor")
    public CompletableFuture<List<Map<String, Object>>> getActiveAndActivatedClients() {
        List<Map<String, Object>> clients = hiveClientRepo.findClientsByStatus();
        return CompletableFuture.completedFuture(clients);
    }

    
    public HiveClient getHiveClientById(Long id) {
        Optional<HiveClient> hiveClients = hiveClientRepo.findById(id);
        return hiveClients.orElse(null);
    }

    public List<HiveClient> getAllSubscriberInfo() {
        return hiveClientRepo.findAll();
    }
    
    public List<HiveClient> getAllSubscriberNetworkInfo() {
        return hiveClientRepo.findAll();
    }
}