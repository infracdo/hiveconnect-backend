package com.autoprov.autoprov.repositories.ipamRepositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.autoprov.autoprov.entity.ipamDomain.CidrBlock;

public interface CidrBlockRepository extends JpaRepository<CidrBlock, Long> {
    boolean existsByCidrBlock(String cidrBlock);
    boolean existsByDefaultGateway(String defaultGateway);
    boolean existsByBroadcastAddress(String broadcastAddress);
    boolean existsByNetworkAddress(String networkAddress);
    boolean existsByNetworkName(String networkName);

   @Query(value = "SELECT * from cidr_block", nativeQuery = true)
   List<CidrBlock>getAllNetworks();

   Optional<CidrBlock> findByCidrBlock(String networkAddress);
}