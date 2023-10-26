package com.autoprov.autoprov.services;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autoprov.autoprov.domain.IpAddress;
import com.autoprov.autoprov.domain.CidrBlock;
import com.autoprov.autoprov.repositories.IpAddressRepository;
import com.autoprov.autoprov.repositories.CidrBlockRepository;

@Service
public class IpListService {

    private static IpAddressRepository ipAddRepo;
    private static CidrBlockRepository cidrBlockRepo;

    @Autowired
    public void IpAddressRepoImpl(IpAddressRepository ipAddRepo) {
        IpListService.ipAddRepo = ipAddRepo;
    }

    @Autowired
    public void cidrBlockRepositoryImpl(CidrBlockRepository cidrBlockRepo) {
        IpListService.cidrBlockRepo = cidrBlockRepo;
    }

    // Functions and Services
    public static String populateIpBycidrBlock(String cidrBlock, Integer internetGateway, Integer hostRange,
            String oltIp, String type, Integer vlanId) {

        // switch (maskBits) {
        // case 24:
        // hostRange = 255;
        // break;
        // case 29:
        // hostRange = 8;
        // break;
        // default:
        // return "CIDR not supported. Only supports /24 and /29";
        // }
        Integer host = 0;
        Integer oltIpHost = 0;

        if (oltIp != null)
            oltIpHost = Integer.parseInt(oltIp);
        else
            oltIpHost = -1;

        while (host <= hostRange) {

            IpAddress ipAdd = IpAddress.builder()
                    .ipAddress(cidrBlock.substring(0, (cidrBlock.lastIndexOf(".") + 1)) + host.toString())
                    .status(defaultRemarks(host, hostRange, internetGateway, oltIpHost)[0])
                    .accountNumber(" ")
                    .type(type)
                    .vlanId(vlanId)
                    .assignable(
                            Boolean.valueOf(defaultRemarks(host, hostRange, internetGateway, oltIpHost)[1]))
                    .notes(defaultRemarks(host, hostRange, internetGateway, oltIpHost)[2])
                    .build();
            ipAddRepo.save(ipAdd);
            host++;
        }
        return "successful";
    }

    public static String addCidrBlock(String cidrBlock, String accountNumber, String internetGateway,
            String oltIp, Integer vlanId, String site,
            String type, String status, String notes) {

        Integer maskBits = Integer.parseInt(cidrBlock.substring((cidrBlock.lastIndexOf("/") + 1)));
        System.out.println(maskBits);
        Integer hostRange = 0;

        if (maskBits == 24) {
            hostRange = 255;
        }
        if (maskBits == 29) {
            hostRange = 8;
        }
        if (maskBits != 24 && maskBits != 29) {
            return "CIDR not supported. Only supports /24 and /29";
        }

        Integer gatewayHost = Integer.parseInt(internetGateway);

        CidrBlock networkAdd = CidrBlock.builder()
                .cidrBlock(cidrBlock)
                .networkAddress(cidrBlock.substring(0, cidrBlock.lastIndexOf("/")))
                .accountNumber(accountNumber)
                .type(type)
                .site(site)
                .vlanId(vlanId)
                .notes(notes)
                .build();
        cidrBlockRepo.save(networkAdd);
        populateIpBycidrBlock(cidrBlock, gatewayHost, hostRange, oltIp, type, vlanId);
        // if (type.equals("Residential") || type.equals("RES")) {
        // populateIpBycidrBlock(cidrBlock, vlanId);
        // }

        return "Successful";
    }

    public static String[] defaultRemarks(Integer host, Integer hostRange, Integer gatewayHost, Integer oltIp) {
        String[] remarks = new String[3]; // [status, assignable, notes]

        System.out.println(gatewayHost);
        if (host == 0) {
            remarks[0] = "Not Available";
            remarks[1] = "false";
            remarks[2] = "Network Address";
        } else if (host.equals(gatewayHost)) {
            remarks[0] = "Not Available";
            remarks[1] = "false";
            remarks[2] = "Internet Gateway";
        } else if (host.equals(oltIp)) {
            remarks[0] = "Not Available";
            remarks[1] = "false";
            remarks[2] = "OLT IP";
        } else if (host.equals(hostRange)) {
            remarks[0] = "Not Available";
            remarks[1] = "false";
            remarks[2] = "Broadcast Address";
        } else {
            remarks[0] = "Available";
            remarks[1] = "true";
            remarks[2] = "Ready to Assign";
        }

        return remarks;
    }
}
