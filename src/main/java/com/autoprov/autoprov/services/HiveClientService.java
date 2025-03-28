package com.autoprov.autoprov.services;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
                .monitoringStatus("unmonitored")
                .build();
        hiveClientRepo.save(newHiveClient);

        return "Successful";
    }

    public static String addHiveMigratedClient(String accountNo, String clientName, String serialNumber,
            String deviceName,
            String macAddress, String status,
            String oltIp,
            String oltInterface, String ipAddress, String provision, String ssidName, String packageType,
            String upstream,
            String downstream) throws Exception {

        HiveClient newHiveClient = HiveClient.builder()
                .subscriberAccountNumber(accountNo)
                .clientName(clientName)
                .onuSerialNumber(serialNumber)
                .onuDeviceName(deviceName)
                .onuMacAddress(macAddress).status(status)
                .oltIp(oltIp)
                .oltInterface(oltInterface)
                .ipAssigned(ipAddress)
                .provision(provision)
                .ssidName(ssidName)
                .packageType(packageType)
                .oltReportedUpstream(upstream)
                .oltReportedDownstream(downstream)
                .monitoringStatus("unmonitored")
                .build();

        Optional<HiveClient> existingSubscriber = hiveClientRepo
                .findBySubscriberAccountNumber(newHiveClient.getSubscriberAccountNumber());
        if (existingSubscriber.isPresent()) {
            throw new Exception("Subscriber already exists.");
        }
        hiveClientRepo.save(newHiveClient);

        return "Successful";
    }

    public List<HiveClient> getAllHiveclients() {
        return hiveClientRepo.findAll();
    }

    public Long getStatusCount(String status) {
        return hiveClientRepo.countClientsByStatus(status);
    }

    public HiveClient getClientByAccountNumber(String subscriberAccountNumber) {
        return hiveClientRepo.findBySubscriberAccountNumber(subscriberAccountNumber).orElse(null);
    }

    public HiveClient getHiveClientByAccountNumber(String subscriberAccountNumber) {
        return hiveClientRepo.findBySubscriberAccountNumber(subscriberAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Subscriber does not exist"));
    }

    public HiveClient getHiveClientNetworkInfo(String subscriberAccountNumber) {
        return hiveClientRepo.findBySubscriberAccountNumber(subscriberAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Subscriber does not exist"));
    }

    @Async("asyncExecutor")
    public List<Map<String, Object>> getActiveAndActivatedClients() {
        List<Map<String, Object>> clients = hiveClientRepo.findClientsByStatus();
        return clients;
    }

    public HiveClient getHiveClientById(Long id) {
        Optional<HiveClient> hiveClients = hiveClientRepo.findById(id);
        return hiveClients.orElse(null);
    }

    public List<HiveClient> getAllSubscriberInfo() {
        return hiveClientRepo.findAll();
    }

    public List<HiveClient> getActiveOnholdSubscribers() {
        return hiveClientRepo.findActiveHold();
    }

    public List<HiveClient> getAllMigratingSubscribers() {
        return hiveClientRepo.findMigrating();
    }

    public List<HiveClient> getAllSubscriberNetworkInfo() {
        return hiveClientRepo.findAll();
    }
}