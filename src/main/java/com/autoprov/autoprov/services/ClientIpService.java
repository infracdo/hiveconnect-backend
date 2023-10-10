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

    public static String addNewClient(String account_No, String client_name, String package_type_id,
            String onu_serial_no,
            String onu_mac_address, String olt_ip) {
        Client newClient = Client.builder()
                .account_No(account_No)
                .client_name(client_name)
                .onu_serial_no(onu_serial_no)
                .onu_mac_address(onu_mac_address)
                .olt_ip(olt_ip)
                .package_type_id(package_type_id)
                .build();
        clientRepo.save(newClient);

        return "Successful";
    }

}
