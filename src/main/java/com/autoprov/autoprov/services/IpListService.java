package com.autoprov.autoprov.services;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autoprov.autoprov.domain.IpAddress;
import com.autoprov.autoprov.domain.NetworkAddress;
import com.autoprov.autoprov.repositories.IpAddressRepository;
import com.autoprov.autoprov.repositories.NetworkAddressRepository;

@Service
public class IpListService {

    private static IpAddressRepository ipAddRepo;
    private static NetworkAddressRepository networkAddressRepo;

    @Autowired
    public void IpAddressRepoImpl(IpAddressRepository ipAddRepo) {
        IpListService.ipAddRepo = ipAddRepo;
    }

    @Autowired
    public void NetworkAddressRepositoryImpl(NetworkAddressRepository networkAddressRepo) {
        IpListService.networkAddressRepo = networkAddressRepo;
    }

    // Functions and Services
    public static String populateIpByNetworkAddress(String networkAddress, Integer vlanId) {
        Integer host = 0;
        while (host <= 255) {

            IpAddress ipAdd = IpAddress.builder()
                    .ipAddress(networkAddress.substring(0, (networkAddress.lastIndexOf(".") + 1)) + host.toString())
                    .status(defaultRemarks(host)[0])
                    .accountNumber(" ")
                    .vlanId(vlanId)
                    .assignable(Boolean.valueOf(defaultRemarks(host)[1]))
                    .notes(defaultRemarks(host)[2])
                    .build();
            ipAddRepo.save(ipAdd);
            host++;
        }
        return "successful";
    }

    public static String addNetworkAddress(String networkAddress, String accountNumber, Integer vlanId, String site,
            String type, String status, String notes) {
        NetworkAddress networkAdd = NetworkAddress.builder()
                .networkAddress(networkAddress)
                .accountNumber(accountNumber)
                .type(type)
                .site(site)
                .vlanId(vlanId)
                .notes(notes)
                .build();
        networkAddressRepo.save(networkAdd);
        populateIpByNetworkAddress(networkAddress, vlanId);
        // if (type.equals("Residential") || type.equals("RES")) {
        // populateIpByNetworkAddress(networkAddress, vlanId);
        // }

        return "Successful";
    }

    public static String[] defaultRemarks(Integer host) {
        String[] remarks = new String[3]; // [status, assignable, notes]

        switch (host) {
            case 0:
                remarks[0] = "Not Available";
                remarks[1] = "false";
                remarks[2] = "Network Address";
                break;
            case 1:
                remarks[0] = "Not Available";
                remarks[1] = "false";
                remarks[2] = "Internet Gateway";
                break;
            case 255:
                remarks[0] = "Not Available";
                remarks[1] = "false";
                remarks[2] = "Broadcast Address";
                break;
            default:
                remarks[0] = "Available";
                remarks[1] = "true";
                remarks[2] = "Ready to Assign";
        }
        return remarks;
    }
}
