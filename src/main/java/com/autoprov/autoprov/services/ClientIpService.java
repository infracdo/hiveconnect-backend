package com.autoprov.autoprov.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autoprov.autoprov.repositories.IpAddressRepository;
import com.autoprov.autoprov.domain.Client;
import com.autoprov.autoprov.domain.CidrBlock;
import com.autoprov.autoprov.repositories.ClientRepository;

@Service
public class ClientIpService {

    private static IpAddressRepository ipAddRepo;
    private static ClientRepository clientRepo;

    @Autowired
    public void IpAddressRepoImpl(IpAddressRepository ipAddRepo) {
        ClientIpService.ipAddRepo = ipAddRepo;
    }

    @Autowired
    public void ClientRepoImpl(ClientRepository clientRepo) {
        ClientIpService.clientRepo = clientRepo;
    }

    // Functions and Services
    public static String associateIpToClient(Long clientId, Long ipAddId) {
        return null;

    }

    public static String addNewClient(String accountNumber, String clientName, String packageTypeId,
            String onuSerialNumber,
            String onuMacAddress, String oltIp) {
        Client newClient = Client.builder()
                .accountNumber(accountNumber)
                .clientName(clientName)
                .onuSerialNumber(onuSerialNumber)
                .onuMacAddress(onuMacAddress)
                .oltIp(oltIp)
                .packageTypeId(packageTypeId)
                .build();
        clientRepo.save(newClient);

        return "Successful";
    }

    public String deleteAllClient() {
        try {
            clientRepo.deleteAll();
            return "Deleted All Client!";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Failed in Deleting";
    }

    public String deleteClientById(Long id) {
        try {
            clientRepo.deleteById(id);
            return "Successfully Deleted Client /w Id: " + id + "!";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Failed in Deleting!";
    }

}
