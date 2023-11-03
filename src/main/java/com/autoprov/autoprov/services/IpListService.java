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
    public static String populateIpBycidrBlock(String cidrBlock, String internetGateway, Integer maskBits,
            String oltIp, String type, Integer vlanId) {

        Integer hostRangeA = 0;
        Integer hostRangeB = 0;
        Integer hostA = 0;
        Integer hostB = 0;

        if (maskBits == 16) {
            hostRangeA = 255;
            hostRangeB = 255;
        }
        if (maskBits == 24) {
            hostRangeA = 255;
        }
        if (maskBits == 29) {
            hostRangeA = 8;
        }

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

        Integer oltIpHost = 0;
        String status = "";
        String notes = "";
        Boolean assignable = true;
        String ipAddress = "";

        if (oltIp != null)
            oltIpHost = Integer.parseInt(oltIp);
        else
            oltIpHost = -1;

        if (hostRangeB == 0) {
            while (hostA <= hostRangeA) {

                ipAddress = cidrBlock.substring(0, (cidrBlock.indexOf(".", cidrBlock.indexOf(".") + 1) + 1))
                        + hostB.toString() + "." + hostA.toString();

                if (ipAddress.equals(internetGateway)) {
                    status = "Not Available";
                    assignable = false;
                    notes = "Internet Gateway";
                } else if (hostA.equals(0)) {
                    status = "Not Available";
                    assignable = false;
                    notes = "Network Address";
                } else if (hostA.equals(hostRangeA)) {
                    status = "Not Available";
                    assignable = false;
                    notes = "Broadcast Address";
                } else {
                    status = "Available";
                    assignable = true;
                    notes = "Ready to assign";
                }

                System.out.println(ipAddress);
                IpAddress ipAdd = IpAddress.builder()
                        .ipAddress(ipAddress)
                        .status(status)
                        .accountNumber(" ")
                        .type(type)
                        .vlanId(vlanId)
                        .assignable(
                                assignable)
                        .notes(notes)
                        .build();
                ipAddRepo.save(ipAdd);
                hostA++;
            }
        } else {
            // str.indexOf(ch, str.indexOf(ch) + 1)

            while (hostB <= hostRangeB) {
                while (hostA <= hostRangeA) {

                    ipAddress = cidrBlock.substring(0, (cidrBlock.indexOf(".", cidrBlock.indexOf(".") + 1) + 1))
                            + hostB.toString() + "." + hostA.toString();

                    if (ipAddress.equals(internetGateway)) {
                        status = "Not Available";
                        assignable = false;
                        notes = "Internet Gateway";
                    } else if (hostA.equals(hostRangeA) && hostB.equals(hostRangeB)) {
                        status = "Not Available";
                        assignable = false;
                        notes = "Broadcast Address";
                    } else {
                        status = "Available";
                        assignable = true;
                        notes = "Ready to assign";
                    }

                    System.out.println(ipAddress);
                    IpAddress ipAdd = IpAddress.builder()
                            .ipAddress(ipAddress)
                            .status(status)
                            .accountNumber(" ")
                            .type(type)
                            .vlanId(vlanId)
                            .assignable(
                                    assignable)
                            .notes(notes)
                            .build();
                    ipAddRepo.save(ipAdd);
                    hostA++;
                }
                hostB++;
                hostA = 0;
            }
}

        return "successful";
    }

    public static String addCidrBlock(String cidrBlock, String accountNumber, String internetGateway,
            String oltIp, Integer vlanId, String site,
            String type, String status, String notes) {

        Integer maskBits = Integer.parseInt(cidrBlock.substring((cidrBlock.lastIndexOf("/") + 1)));
        System.out.println(maskBits);
        Integer hostRange = 0;

        if (maskBits == 24 || maskBits == 29 || maskBits == 16)
            System.out.println("Populating...");
        else
            return "CIDR not supported. Only supports /16, /24 and /29";

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
        populateIpBycidrBlock(cidrBlock, internetGateway, maskBits, oltIp, type, vlanId);
        // if (type.equals("Residential") || type.equals("RES")) {
        // populateIpBycidrBlock(cidrBlock, vlanId);
        // }

        return "Successful";
    }

    // public static String[] defaultRemarks(Integer host, Integer hostRange,
    // Integer gatewayHost, Integer oltIp) {
    // String[] remarks = new String[3]; // [status, assignable, notes]

    // System.out.println(gatewayHost);
    // if (host == 0) {
    // remarks[0] = "Not Available";
    // remarks[1] = "false";
    // remarks[2] = "Network Address";
    // } else if (host.equals(gatewayHost)) {
    // remarks[0] = "Not Available";
    // remarks[1] = "false";
    // remarks[2] = "Internet Gateway";
    // } else if (host.equals(oltIp)) {
    // remarks[0] = "Not Available";
    // remarks[1] = "false";
    // remarks[2] = "OLT IP";
    // } else if (host.equals(hostRange)) {
    // remarks[0] = "Not Available";
    // remarks[1] = "false";
    // remarks[2] = "Broadcast Address";
    // } else {
    // remarks[0] = "Available";
    // remarks[1] = "true";
    // remarks[2] = "Ready to Assign";
    // }

    // return remarks;
    // }
}
