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
        Boolean assignable = null;
        String notes = null;
        String status = null;
        while (host <= 255) {

            switch (host) {
                case 0:
                    notes = "Network Address";
                    status = "Not Assignable";
                    assignable = false;
                    break;
                case 1:
                    notes = "Internet Gateway";
                    status = "Not Assignable";
                    assignable = false;
                case 255:
                    notes = "Broadcast Address";
                    status = "Not Assignable";
                    assignable = false;
                    break;
                default:
                    notes = "Ready to assign";
                    status = "Available";
                    assignable = true;
            }

            IpAddress ipAdd = IpAddress.builder()
                    .ipAddress(NetworkAddress.substring(0, (NetworkAddress.lastIndexOf(".") + 1)) + host.toString())
                    .status(status)
                    .clientId(" ")
                    .vlanId(0)
                    .assignable(assignable)
                    .notes(notes)
                    .build();
            ipAddRepo.save(ipAdd);
            host++;
        }
        return "successful";
    }
}
