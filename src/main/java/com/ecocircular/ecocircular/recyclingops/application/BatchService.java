package com.ecocircular.ecocircular.recyclingops.application;

import com.ecocircular.ecocircular.recyclingops.domain.Batch;
import com.ecocircular.ecocircular.recyclingops.infrastructure.persistence.BatchRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BatchService {

    private final BatchRepository repository;

    public BatchService(BatchRepository repository) {
        this.repository = repository;
    }

    public Batch create(Batch batch) {
        return repository.save(batch);
    }
    public Batch read(UUID id) {
        return repository.findById(id)
                .orElseThrow();
    }

    public Batch dispatch(UUID id) {
        Batch batch = repository.findById(id)
                .orElseThrow();

        batch.dispatch();

        return repository.save(batch);
    }

    public Batch receive(UUID id) {
        Batch batch = repository.findById(id)
                .orElseThrow();

        batch.receive();

        return repository.save(batch);
    }
}