// package com.autoprov.autoprov.services;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import java.util.List;
// import java.util.stream.Collectors;

// import com.autoprov.autoprov.dto.NetworkDataResponse;
// import com.autoprov.autoprov.entity.ipamDomain.CidrBlock;
// import com.autoprov.autoprov.entity.oltDomain.oltEntity;
// import com.autoprov.autoprov.repositories.ipamRepositories.CidrBlockRepository;
// import com.autoprov.autoprov.repositories.oltRepositories.oltRepository;

// @Service
// public class NetworkService {

//     @Autowired
//     private CidrBlockRepository cidrBlockRepository;

//     @Autowired
//     private oltRepository oltEntityRepository;

//     public List<NetworkDataResponse> getAllNetworkData() {
//         List<CidrBlock> cidrBlocks = cidrBlockRepository.findAll();

//         return cidrBlocks.stream().map(cidrBlock -> {
//             List<oltEntity> oltEntities = oltEntityRepository.findByOltNetworksite(cidrBlock.getNetworkName());
//             return new NetworkDataResponse(cidrBlock.getNetworkName(), oltEntities);
//         }).collect(Collectors.toList());
//     }
// }