package com.autoprov.autoprov.services;

import org.springframework.beans.factory.annotation.Autowired;
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
            String oltInterface, String ipAddress, String ssidName, String packageTypeId, String upstream,
            String downstream) {

        HiveClient newHiveClient = HiveClient.builder()
                .accountNumber(accountNo)
                .clientName(clientName)
                .onuSerialNumber(serialNumber)
                .onuDeviceName(deviceName)
                .onuMacAddress(macAddress).status("Activated")
                .oltIp(oltIp)
                .oltInterface(oltInterface)
                .ipAssigned(ipAddress)
                .backend("HiveConnect")
                .ssidName(ssidName)
                .packageTypeId(packageTypeId)
                .oltReportedUpstream(upstream)
                .oltReportedDownstream(downstream)
                .build();
        hiveClientRepo.save(newHiveClient);

        return "Successful";
    }
}
