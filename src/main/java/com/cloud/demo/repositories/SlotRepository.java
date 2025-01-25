package com.cloud.demo.repositories;

import com.cloud.demo.models.Slot;

import java.util.List;
import com.cloud.demo.models.SlotStatus;

import reactor.core.publisher.Flux;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SlotRepository extends JpaRepository<Slot, Long> {
    Flux <Slot> findByStatus (SlotStatus status); 

}