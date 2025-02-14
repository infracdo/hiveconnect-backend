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

    // Now allows duplicate oltName and oltIp entries
    public oltEntity createOlt(oltEntity oltEntity) {
        return oltRepository.save(oltEntity);
    }

    public Optional<oltEntity> getOltByName(String oltName) {
        return oltRepository.findByOltName(oltName);
    }

    public Optional<oltEntity> getOltByIp(String oltIp) {
        return oltRepository.findByOltIp(oltIp);
    }

    public List<oltEntity> getAllOlts() {
        return oltRepository.getAllOlts();
    }
}
