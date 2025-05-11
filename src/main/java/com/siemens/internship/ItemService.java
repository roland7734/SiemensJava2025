package com.siemens.internship;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Handles business logic for Item entities, including
 * basic CRUD operations and asynchronous processing.
 */
@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    // Thread pool used for concurrent item processing
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    // Retrieve all items from the database
    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    // Retrieve a specific item by ID
    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    // Save or update an item in the database
    public Item save(Item item) {
        return itemRepository.save(item);
    }

    // Delete an item by its ID
    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }

    /**
     * Asynchronously process all items by:
     * - Fetching all item IDs
     * - Updating each item's status to "PROCESSED"
     * - Saving changes back to the database
     * - Returning the list of successfully processed items
     */
    @Async
    public List<Item> processItemsAsync() {
        List<Long> itemIds = itemRepository.findAllIds();

        // Thread-safe collection to hold processed items
        ConcurrentLinkedQueue<Item> processedItems = new ConcurrentLinkedQueue<>();

        // Launch async tasks for each item
        List<CompletableFuture<Void>> futures = itemIds.stream()
                .map(id -> CompletableFuture.runAsync(() -> {
                    try {
                        itemRepository.findById(id).ifPresent(item -> {
                            item.setStatus("PROCESSED");
                            Item saved = itemRepository.save(item);
                            processedItems.add(saved);
                        });
                    } catch (Exception e) {
                        System.err.println("Error processing item ID " + id + ": " + e.getMessage());
                    }
                }, executor))
                .collect(Collectors.toList());

        // Wait for all async operations to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return new ArrayList<>(processedItems);
    }
}
