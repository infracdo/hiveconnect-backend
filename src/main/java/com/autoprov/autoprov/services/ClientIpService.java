package com.autoprov.autoprov.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autoprov.autoprov.entity.inetDomain.Client;
import com.autoprov.autoprov.entity.ipamDomain.CidrBlock;
import com.autoprov.autoprov.repositories.inetRepositories.ClientRepository;
import com.autoprov.autoprov.repositories.ipamRepositories.IpAddressRepository;

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
