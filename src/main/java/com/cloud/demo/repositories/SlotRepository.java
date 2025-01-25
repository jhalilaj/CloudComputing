package com.cloud.demo.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cloud.demo.models.Slot;
import com.cloud.demo.models.SlotStatus;

public interface SlotRepository extends JpaRepository<Slot, Long> {
    List<Slot> findByStatus(SlotStatus status);
}
