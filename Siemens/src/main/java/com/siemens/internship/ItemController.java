package com.siemens.internship;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return ResponseEntity.ok(itemService.findAll());
    }

    @PostMapping
    public ResponseEntity<?> createItem(@Valid @RequestBody ItemDTO itemDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        Item item = new Item(null, itemDTO.getName(), itemDTO.getDescription(), itemDTO.getStatus(), itemDTO.getEmail());
        return new ResponseEntity<>(itemService.save(item), HttpStatus.CREATED);
    }


    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return itemService.findById(id)
                .map(item -> ResponseEntity.ok(item))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(@PathVariable Long id, @Valid @RequestBody ItemDTO itemDTO) {
        Optional<Item> existingItem = itemService.findById(id);
        if (existingItem.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item not found");
        }
        Item updated = new Item(id, itemDTO.getName(), itemDTO.getDescription(), itemDTO.getStatus(), itemDTO.getEmail());
        return new ResponseEntity<>(itemService.save(updated), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        if (itemService.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        itemService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/process")
    public ResponseEntity<List<Item>> processItems() {
        List<Item> processed = itemService.processItemsAsync();
        return new ResponseEntity<>(processed, HttpStatus.OK);
    }
}
