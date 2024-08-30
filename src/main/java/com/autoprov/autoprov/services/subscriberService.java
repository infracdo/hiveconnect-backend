package com.autoprov.autoprov.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autoprov.autoprov.entity.subscriberDomain.subscriberEntity;
import com.autoprov.autoprov.repositories.subscriberRepositories.subscriberRepository;

import jakarta.transaction.Transactional;


@Service
@Transactional
public class subscriberService {

    @Autowired
    private subscriberRepository SubscriberRepo;

    public subscriberEntity saveSubscriber(subscriberEntity subscriber) throws Exception {
        Optional<subscriberEntity> existingSubscriber = SubscriberRepo.findBySubscriberAccountNumber
        (subscriber.getSubscriberAccountNumber());
        if (existingSubscriber.isPresent()) {
            throw new Exception("Subscriber with this ACCOUNT number already exists.");
        }
        return SubscriberRepo.save(subscriber);
    }

    public List<subscriberEntity> getAllSubscribers() {
        return SubscriberRepo.getNewClients();
    }

    public List<subscriberEntity> getProvisionedSubscribers() {
        return SubscriberRepo.getNonNewClients();
    }


    public subscriberEntity getSubscriberById(Long id) {
        Optional<subscriberEntity> subscriber = SubscriberRepo.findById(id);
        return subscriber.orElse(null);
    }

   
    
    public subscriberEntity getSubscriberAccountInfo(String subscriberAccountNumber) {
        return SubscriberRepo.findBySubscriberAccountNumber(subscriberAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("subscriber account number does not exist: " + subscriberAccountNumber));
    }
    
}