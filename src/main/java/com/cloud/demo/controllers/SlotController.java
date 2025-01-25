package com.cloud.demo.controllers;


import com.cloud.demo.repositories.SlotRepository;
import com.cloud.demo.models.Slot;
import com.cloud.demo.models.SlotStatus;
import org.springframework.hateoas.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid; // Updated import
import java.util.*;
import java.util.stream.Collectors;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/slots")
public class SlotController {
    private final SlotRepository slotRepository;

    public SlotController(SlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    @PostMapping
    public ResponseEntity<EntityModel<Slot>> createSlot(@Valid @RequestBody Slot slot) {
        slot.setStatus(SlotStatus.AVAILABLE);
        Slot savedSlot = slotRepository.save(slot);
        return ResponseEntity.created(linkTo(methodOn(SlotController.class).getSlot(savedSlot.getId())).toUri())
                .body(EntityModel.of(savedSlot, getLinks(savedSlot)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Slot>> getSlot(@PathVariable Long id) {
        return slotRepository.findById(id)
                .map(slot -> ResponseEntity.ok(EntityModel.of(slot, getLinks(slot))))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Slot>>> getAllSlots(
            @RequestParam(required = false) SlotStatus status) {
        List<Slot> slots = (status == null) ? slotRepository.findAll() : slotRepository.findByStatus(status);
        List<EntityModel<Slot>> slotModels = slots.stream()
                .map(slot -> EntityModel.of(slot, getLinks(slot)))
                .collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(slotModels,
                linkTo(methodOn(SlotController.class).getAllSlots(status)).withSelfRel()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSlot(@PathVariable Long id, @Valid @RequestBody Slot updatedSlot) {
        return slotRepository.findById(id)
                .map(existingSlot -> {
                    existingSlot.setDate(updatedSlot.getDate());
                    existingSlot.setTime(updatedSlot.getTime());
                    existingSlot.setRoomNumber(updatedSlot.getRoomNumber());
                    existingSlot.setRegistrarName(updatedSlot.getRegistrarName());
                    existingSlot.setStatus(updatedSlot.getStatus());
                    Slot savedSlot = slotRepository.save(existingSlot);
                    return ResponseEntity.ok(EntityModel.of(savedSlot, getLinks(savedSlot)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> partiallyUpdateSlot(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return slotRepository.findById(id)
                .map(existingSlot -> {
                    if (updates.containsKey("status")) {
                        existingSlot.setStatus(SlotStatus.valueOf((String) updates.get("status")));
                    }
                    Slot savedSlot = slotRepository.save(existingSlot);
                    return ResponseEntity.ok(EntityModel.of(savedSlot, getLinks(savedSlot)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSlot(@PathVariable Long id) {
        return slotRepository.findById(id)
                .map(slot -> {
                    slotRepository.delete(slot);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private Links getLinks(Slot slot) {
        List<Link> links = new ArrayList<>();
        links.add(linkTo(methodOn(SlotController.class).getSlot(slot.getId())).withSelfRel());
        links.add(linkTo(methodOn(SlotController.class).updateSlot(slot.getId(), null)).withRel("update"));
        links.add(linkTo(methodOn(SlotController.class).deleteSlot(slot.getId())).withRel("delete"));
        if (slot.getStatus() == SlotStatus.AVAILABLE) {
            links.add(linkTo(methodOn(SlotController.class).partiallyUpdateSlot(slot.getId(), null)).withRel("book"));
        } else {
            links.add(linkTo(methodOn(SlotController.class).partiallyUpdateSlot(slot.getId(), null)).withRel("cancel"));
        }
        return Links.of(links);
    }
}