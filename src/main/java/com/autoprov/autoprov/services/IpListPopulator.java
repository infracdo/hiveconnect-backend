package com.autoprov.autoprov.services;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autoprov.autoprov.domain.IpAddress;
import com.autoprov.autoprov.repositories.IpAddressRepo;

@Service
public class IpListPopulator {

    private static IpAddressRepo ipAddRepo;

    @Autowired
    public void IpAddressRepoImpl(IpAddressRepo ipAddRepo) {
        IpListPopulator.ipAddRepo = ipAddRepo;
    }

    public static String populateIpByNetworkAddress(String NetworkAddress) {
        Integer host = 0;
        while (host <= 256) {
            IpAddress ipAdd = IpAddress.builder()
                    .networkAddress(NetworkAddress)
                    .hostAddress("0.0.0." + host.toString())
                    .status("Available")
                    .clientId(" ")
                    .vlanId(0)
                    .build();
            ipAddRepo.save(ipAdd);
            host++;
        }
        return "successful";
    }
}
