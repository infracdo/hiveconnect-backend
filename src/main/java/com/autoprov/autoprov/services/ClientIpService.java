package com.autoprov.autoprov.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autoprov.autoprov.repositories.IpAddressRepository;
import com.autoprov.autoprov.domain.Client;
import com.autoprov.autoprov.domain.NetworkAddress;
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

    public static String addNewClient(String account_No, String package_type_id) {
        Client newClient = Client.builder()
                .account_No(account_No)
                .ip_assigned("None")
                .onu_serial_no("None")
                .package_type_id(package_type_id)
                .build();
        clientRepo.save(newClient);

        return "Successful";
    }

}
