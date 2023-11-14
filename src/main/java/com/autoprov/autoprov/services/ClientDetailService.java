package com.autoprov.autoprov.services;

import org.springframework.beans.factory.annotation.Autowired;

import com.autoprov.autoprov.entity.inetDomain.ClientDetail;
import com.autoprov.autoprov.repositories.inetRepositories.ClientDetailRepository;

public class ClientDetailService {
    private static ClientDetailRepository clientDetailRepo;

    @Autowired
    public void ClientDetailRepoImpl(ClientDetailRepository clientDetailRepo) {
        ClientDetailService.clientDetailRepo = clientDetailRepo;
    }

    public void updateClientDetail(){
        ClientDetail updateClient = clientDetailRepo.builder().

    }
}
