package org.example.application.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.example.application.model.Item;
import org.springframework.stereotype.Service;

@Service
public class ItemService {

    private final Map<Long, Item> items = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public ItemService() {
        create("Template item A");
        create("Template item B");
    }

    public List<Item> findAll() {
        return List.copyOf(items.values());
    }

    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    public Item create(String name) {
        Item item = new Item(sequence.incrementAndGet(), name);
        items.put(item.id(), item);
        return item;
    }
}
