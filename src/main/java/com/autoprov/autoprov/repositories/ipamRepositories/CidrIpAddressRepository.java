package com.autoprov.autoprov.repositories.ipamRepositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.autoprov.autoprov.entity.ipamDomain.CidrIpAddress;

import jakarta.transaction.Transactional;

public interface CidrIpAddressRepository extends JpaRepository<CidrIpAddress, Long> {
    
    @Query(value = "SELECT * from cidr_ipaddress where status = \"Available\"", nativeQuery = true)
    List<CidrIpAddress> findAllAvailableIp();

    @Query(value = "SELECT * from cidr_ipaddress where ipAddress LIKE ?1%", nativeQuery = true)
    //@Query(value = "SELECT * from cidr_ipaddress", nativeQuery = true)
    List<CidrIpAddress> findAllUnderCidrBlock(String cidrBlock);

  
    @Query(value = "SELECT * from cidr_ipaddress where network_address LIKE ?1%", nativeQuery = true)
    List<CidrIpAddress> findAllByNetworkAddress(String networkAddress);
    
    @Query(value = "SELECT ipAddress from cidr_ipaddress where ipAddress LIKE ?1% AND notes LIKE '%OLT IP%'", nativeQuery = true)
    String getOltIpOfIpAddress(String cidrBlock);

    @Query(value = "SELECT ipAddress from cidr_ipaddress where ipAddress LIKE ?1% AND notes LIKE '%Default Gateway%'", nativeQuery = true)
    String getGatewayOfIpAddress(String cidrBlock);

    @Modifying
    @Transactional
    @Query("update CidrIpAddress u set u.accountNumber = ?1, u.status = \'Assigned\' , u.notes = \'Assigned to client\' where u.ipAddress = ?2")
    void associateIpAddressToAccountNumber(String accountNumber, String ipAddress);

    @Modifying
    @Transactional
    @Query("update CidrIpAddress u set u.accountNumber = ?1, u.status = \'Reserved\' where u.ipAddress = ?2")
    void reserveIpAddressToAccountNumber(String accountNumber, String ipAddress);

    @Modifying
    @Transactional
    @Query(value = "DELETE from cidr_ipaddress where ipAddress LIKE ?1%", nativeQuery = true)
    void deleteIpAddressUnderCidrBlock(String cidrBlock);

    @Query(value = "SELECT * from cidr_ipaddress where status =\"Available\" AND ipAddress LIKE ?1% LIMIT 1", nativeQuery = true)
    List<CidrIpAddress> getOneAvailableIpAddressUnderCidrBlock(String cidrBlock);

    // @Query(value = "SELECT * from hive.ipaddresses where status = \"Available\"
    // and ip_address LIKE\n" + //
    // "(\n" + //
    // "\tSELECT CONCAT(truncated_network_address, '%') FROM\n" + //
    // "\t(\n" + //
    // "\tSELECT \n" + //
    // "\t SUBSTRING_INDEX(network_address, '.', 3) AS truncated_network_address\n"
    // + //
    // "\tFROM \n" + //
    // "\t hive.cidr_block \n" + //
    // "\tWHERE \n" + //
    // "\t site = ?1 AND network_type = \"Private\"\n" + //
    // "\t) AS subquery\n" + //
    // ")\n" + //
    // "LIMIT 1;", nativeQuery = true)
    @Query(value = "SELECT * FROM cidr_ipaddress " +
            "WHERE status = 'Available' " +
            "AND ipAddress LIKE (" +
            "  SELECT CONCAT(SUBSTRING_INDEX(network_address, '.', 3), '%') AS pattern " +
            "  FROM cidr_block " +
            "  WHERE network_name = ?1 AND network_type = ?2" +
            "  ORDER BY CAST(ipAddress AS SIGNED)" +
            ") " +
            "LIMIT 1", nativeQuery = true)
    List<CidrIpAddress> getOneAvailableIpAddressUnderSite(String site, String type);

    @Query(value = "SELECT * from cidr_ipaddress where status =\"Available\" AND type = ?1 ipAddress LIKE ?2% LIMIT 1", nativeQuery = true)
    List<CidrIpAddress> getOneAvailableIpAddressUnderCidrBlockAndType(String type, String cidrBlock);

    @Modifying
    @Transactional
    @Query(value = "DELETE from cidr_block where network_address LIKE ?1%", nativeQuery = true)
    void deleteCidrBlock(String cidrBlock);

    @Query(value = "SELECT * from cidr_ipaddress where ipAddress=?1", nativeQuery = true)
    Optional<CidrIpAddress> findByipAddress(String ipAddress);

    @Query(value = "SELECT * from cidr_ipaddress where status =\"Available\" LIMIT 1", nativeQuery = true)
    List<CidrIpAddress> getOneAvailableIpAddress();

}