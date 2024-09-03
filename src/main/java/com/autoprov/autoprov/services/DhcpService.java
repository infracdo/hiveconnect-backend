package com.autoprov.autoprov.services;


import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autoprov.autoprov.dto.CidrBlockDTO;
import com.autoprov.autoprov.entity.ipamDomain.CidrBlock;
import com.autoprov.autoprov.entity.ipamDomain.CidrIpAddress;
import com.autoprov.autoprov.exception.CidrBlockAlreadyExistsException;
import com.autoprov.autoprov.repositories.ipamRepositories.CidrBlockRepository;
import com.autoprov.autoprov.repositories.ipamRepositories.CidrIpAddressRepository;

@Service
public class DhcpService {

    @Autowired
    private CidrBlockRepository cidrBlockRepository;

    @Autowired
    private CidrIpAddressRepository cidrIpAddressRepository;

  

    public void createNetwork(CidrBlockDTO cidrBlockDTO) {
        if (cidrBlockRepository.existsByCidrBlock(cidrBlockDTO.getCidrBlock())) {
            throw new CidrBlockAlreadyExistsException("CIDR Block already exists: " + cidrBlockDTO.getCidrBlock());
        }
        if (cidrBlockRepository.existsByDefaultGateway(cidrBlockDTO.getDefaultGateway())) {
            throw new CidrBlockAlreadyExistsException("CIDR default gateway already exists: " + cidrBlockDTO.getDefaultGateway());
        }
        if (cidrBlockRepository.existsByBroadcastAddress(cidrBlockDTO.getBroadcastAddress())) {
            throw new CidrBlockAlreadyExistsException("CIDR Broadcast Address already exists: " + cidrBlockDTO.getBroadcastAddress());
        }
        if (cidrBlockRepository.existsByNetworkAddress(cidrBlockDTO.getNetworkAddress())) {
            throw new CidrBlockAlreadyExistsException("CIDR Network Address already exists: " + cidrBlockDTO.getNetworkAddress());
        }
        if (cidrBlockRepository.existsByNetworkName(cidrBlockDTO.getNetworkName())) {
            throw new CidrBlockAlreadyExistsException("CIDR Network Name already exists: " + cidrBlockDTO.getNetworkName());
        }



        CidrBlock cidrBlock = new CidrBlock(null, null, null, null, null, null, null, null, null);
        cidrBlock.setCidrBlock(cidrBlockDTO.getCidrBlock());
        cidrBlock.setDefaultGateway(cidrBlockDTO.getDefaultGateway());
        cidrBlock.setBroadcastAddress(cidrBlockDTO.getBroadcastAddress());
        cidrBlock.setNetworkAddress(cidrBlockDTO.getNetworkAddress());
        cidrBlock.setNetworkName(cidrBlockDTO.getNetworkName());
        cidrBlock.setNetworkType(cidrBlockDTO.getNetworkType());
        cidrBlock.setVlanId(cidrBlockDTO.getVlanId());
        cidrBlock.setLocation(cidrBlockDTO.getLocation());
        cidrBlockRepository.save(cidrBlock);

        List<CidrIpAddress> ipAddresses = generateIpAddresses(cidrBlockDTO);
        cidrIpAddressRepository.saveAll(ipAddresses);
    }

    
    private List<CidrIpAddress> generateIpAddresses(CidrBlockDTO cidrBlockDTO) {
        List<CidrIpAddress> ipAddresses = new ArrayList<>();
        String cidrBlock = cidrBlockDTO.getCidrBlock();
        String networkType = cidrBlockDTO.getNetworkType();
        String defaultGateway = cidrBlockDTO.getDefaultGateway();
        String broadcastAddress = cidrBlockDTO.getBroadcastAddress();
        String networkAddress = cidrBlockDTO.getNetworkAddress();
        String vlanId = cidrBlockDTO.getVlanId();

        // Parse CIDR block
        String[] parts = cidrBlock.split("/");
        String network = parts[0];
        int prefix = Integer.parseInt(parts[1]);

        int subnetSize = 32 - prefix;
        int numberOfAddresses = (int) Math.pow(2, subnetSize);

        // Convert network address to integer
        long networkAddressInt = ipToLong(network);
        long endAddressInt = networkAddressInt + numberOfAddresses - 1;

        for (long addressInt = networkAddressInt; addressInt <= endAddressInt; addressInt++) {
            CidrIpAddress ipAddress = new CidrIpAddress();
            String ip = longToIp(addressInt);
            ipAddress.setIpAddress(ip);
            ipAddress.setAccountNumber(null); // Set to null as per requirement
            ipAddress.setNotes("Ready to assign");
            ipAddress.setNetworkAddress(networkAddress);
            ipAddress.setType(networkType);
            ipAddress.setVlanId(vlanId); // Set the vlanId here


            if (ip.equals(defaultGateway)) {
                ipAddress.setStatus("Not Available");
                ipAddress.setNotes("Default Gateway");
            } else if (ip.equals(broadcastAddress)) {
                ipAddress.setStatus("Not Available");
                ipAddress.setNotes("Broadcast Address");
            } 
            else if (ip.equals(networkAddress)) {
                ipAddress.setStatus("Not Available");
                ipAddress.setNotes("Network Address");
            }
            else {
                ipAddress.setStatus("Available");
            }

            ipAddresses.add(ipAddress);
        }

        return ipAddresses;
    }

    private long ipToLong(String ipAddress) {
        String[] octets = ipAddress.split("\\.");
        long result = 0;
        for (int i = 0; i < octets.length; i++) {
            result <<= 8;
            result |= Integer.parseInt(octets[i]);
        }
        return result;
    }

    private String longToIp(long longIp) {
        return (longIp >> 24 & 0xFF) + "." +
               (longIp >> 16 & 0xFF) + "." +
               (longIp >> 8 & 0xFF) + "." +
               (longIp & 0xFF);
    }



    public List<CidrBlock> getAllCidrBlocks() {
        return cidrBlockRepository.findAll();
    }

    public List<CidrIpAddress> getAllCidrIpAddresses() {
        return cidrIpAddressRepository.findAll();
    }

}