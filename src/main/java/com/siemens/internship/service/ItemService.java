package com.siemens.internship.service;

import com.siemens.internship.repository.ItemRepository;
import com.siemens.internship.model.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);
    private final List<Item> processedItems = new ArrayList<>();
    private int processedCount = 0;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

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
        if (!itemRepository.existsById(id)) {
            throw new RuntimeException("Item not found with id: " + id);
        }
        itemRepository.deleteById(id);
    }


    /**
     * Your Tasks
     * Identify all concurrency and asynchronous programming issues in the code
     * Fix the implementation to ensure:
     * All items are properly processed before the CompletableFuture completes
     * Thread safety for all shared state
     * Proper error handling and propagation
     * Efficient use of system resources
     * Correct use of Spring's @Async annotation
     * Add appropriate comments explaining your changes and why they fix the issues
     * Write a brief explanation of what was wrong with the original implementation
     * <p>
     * Hints
     * Consider how CompletableFuture composition can help coordinate multiple async operations
     * Think about appropriate thread-safe collections
     * Examine how errors are handled and propagated
     * Consider the interaction between Spring's @Async and CompletableFuture
     */

    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {
        List<Long> itemIds = itemRepository.findAllIds();
        //use a list to hold futures
        List<CompletableFuture<Item>> futures = new ArrayList<>();

        for (Long id : itemIds) {
            //submit each task asynchronously
            CompletableFuture<Item> future = CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(100);
                    Optional<Item> optionalItem = itemRepository.findById(id);
                    if (optionalItem.isPresent()) {
                        Item item = optionalItem.get();
                        item.setStatus("PROCESSED");
                        incrementProcessedCount(); //increment counter in a thread-safe way
                        return itemRepository.save(item);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to process item " + id + ": " + e.getMessage());
                }
                return null;
            });
            futures.add(future);
        }

        //wait for all tasks to complete
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<Item> processedItems = new ArrayList<>();
                    for (CompletableFuture<Item> future : futures) {
                        Item item = future.join(); //here join each future
                        if (item != null) {
                            processedItems.add(item);
                        }
                    }
                    return processedItems;
                });
    }

    private synchronized void incrementProcessedCount() {
        processedCount++;
    }

//    @Async
//    public CompletableFuture<List<Item>> processItemsAsync() {
//
//        List<Long> itemIds = itemRepository.findAllIds();
//
//
//        for (Long id : itemIds) {
//            CompletableFuture.runAsync(() -> {
//                try {
//                    Thread.sleep(100);
//
//                    Item item = itemRepository.findById(id).orElse(null);
//                    if (item == null) {
//                        return;
//                    }
//
//                    processedCount++;
//
//                    item.setStatus("PROCESSED");
//                    itemRepository.save(item);
//                    processedItems.add(item); //this is not thread safe
//
//                } catch (InterruptedException e) {
//                    System.out.println("Error: " + e.getMessage());
//                }
//            }, executor);
//        }
//
//        return CompletableFuture.completedFuture(processedItems); //this returns immediately
//    }

}

