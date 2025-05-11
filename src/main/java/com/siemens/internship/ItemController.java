package com.siemens.internship;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST API controller for managing Item entities.
 * Provides endpoints for CRUD operations and async processing.
 */
@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    /**
     * Fetch all items.
     */
    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return ResponseEntity.ok(itemService.findAll());
    }

    /**
     * Create a new item.
     * Validates the incoming DTO before saving.
     */
    @PostMapping
    public ResponseEntity<?> createItem(@Valid @RequestBody ItemDTO itemDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            // Return 400 Bad Request if validation fails
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        Item item = new Item(null, itemDTO.getName(), itemDTO.getDescription(), itemDTO.getStatus(), itemDTO.getEmail());
        return new ResponseEntity<>(itemService.save(item), HttpStatus.CREATED);
    }

    /**
     * Retrieve a single item by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return itemService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Update an existing item.
     * If the item doesn't exist, return 404.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(@PathVariable Long id, @Valid @RequestBody ItemDTO itemDTO) {
        Optional<Item> existingItem = itemService.findById(id);
        if (existingItem.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item not found");
        }

        Item updated = new Item(id, itemDTO.getName(), itemDTO.getDescription(), itemDTO.getStatus(), itemDTO.getEmail());
        return new ResponseEntity<>(itemService.save(updated), HttpStatus.OK);
    }

    /**
     * Delete an item by ID.
     * Returns 404 if the item does not exist.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        if (itemService.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        itemService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Process all items asynchronously.
     * Updates status to "PROCESSED" and returns list of affected items.
     */
    @GetMapping("/process")
    public ResponseEntity<List<Item>> processItems() {
        List<Item> processed = itemService.processItemsAsync();
        return new ResponseEntity<>(processed, HttpStatus.OK);
    }
}
