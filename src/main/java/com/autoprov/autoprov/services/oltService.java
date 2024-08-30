package com.autoprov.autoprov.services;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autoprov.autoprov.entity.oltDomain.oltEntity;
import com.autoprov.autoprov.repositories.oltRepositories.oltRepository;

@Service
public class oltService {

    @Autowired
    private oltRepository oltRepository;

    public oltEntity createOlt(oltEntity oltEntity) throws Exception {
        Optional<oltEntity> existingByName = oltRepository.findByOltName(oltEntity.getOltName());
        if (existingByName.isPresent()) {
            throw new Exception("olt name already exists:" + oltEntity.getOltName());
        }

        Optional<oltEntity> existingByIp = oltRepository.findByOltIp(oltEntity.getOltIp());
        if (existingByIp.isPresent()) {
            throw new Exception("olt ip already exists:" + oltEntity.getOltIp());
        }

        return oltRepository.save(oltEntity);
    }


    public Optional<oltEntity> getOltByName(String oltName) {
        return oltRepository.findByOlt_Name(oltName);
    }

    public Optional<oltEntity> getOltByIp(String oltIp) {
        return oltRepository.findByOlt_ip(oltIp);
    }

    public List<oltEntity> getAllOlts() {
        return oltRepository.getAllOlts();
    }
}
