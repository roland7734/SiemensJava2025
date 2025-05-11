package com.siemens.internship;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }

    /**
     * Asynchronously processes all items:
     * 1. Fetches all item IDs.
     * 2. Concurrently retrieves and updates each item.
     * 3. Collects all successfully processed items.
     * 4. Returns the list when all processing is complete.
     */
    @Async
    public List<Item> processItemsAsync() {
        List<Long> itemIds = itemRepository.findAllIds();

        ConcurrentLinkedQueue<Item> processedItems = new ConcurrentLinkedQueue<>();
        List<CompletableFuture<Void>> futures = itemIds.stream()
                .map(id -> CompletableFuture.runAsync(() -> {
                    try {
                        Optional<Item> itemOpt = itemRepository.findById(id);
                        itemOpt.ifPresent(item -> {
                            item.setStatus("PROCESSED");
                            Item saved = itemRepository.save(item);
                            processedItems.add(saved);
                        });
                    } catch (Exception e) {
                        // Log exception properly
                        System.err.println("Error processing item ID " + id + ": " + e.getMessage());
                    }
                }, executor))
                .collect(Collectors.toList());

        // Wait for all tasks to finish
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return new ArrayList<>(processedItems);
    }
}
