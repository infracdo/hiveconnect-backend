package com.autoprov.autoprov.repositories.ipamRepositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.autoprov.autoprov.entity.ipamDomain.CidrBlock;

public interface CidrBlockRepository extends CrudRepository<CidrBlock, Long> {

    Optional<CidrBlock> findByCidrBlock(String networkAddress);

}
