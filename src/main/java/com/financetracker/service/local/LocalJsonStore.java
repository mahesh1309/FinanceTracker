package com.financetracker.service.local;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.financetracker.model.local.HasId;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class LocalJsonStore {

    private final ObjectMapper mapper;
    private final Path dataDir;

    public LocalJsonStore() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        dataDir = Paths.get("local-data");
        try {
            Files.createDirectories(dataDir);
        } catch (IOException ignored) {}
    }

    public <T extends HasId> List<T> readAll(String storeName, Class<T> type) {
        Path file = dataDir.resolve(storeName + ".json");
        if (!Files.exists(file)) return new ArrayList<>();
        try {
            return mapper.readValue(file.toFile(),
                mapper.getTypeFactory().constructCollectionType(List.class, type));
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public <T extends HasId> void save(String storeName, T item, Class<T> type) {
        List<T> all = readAll(storeName, type);
        String id = item.getId();
        all.removeIf(e -> id.equals(e.getId()));
        all.add(item);
        writeAll(storeName, all);
    }

    public <T extends HasId> void deleteById(String storeName, String id, Class<T> type) {
        List<T> all = readAll(storeName, type);
        all.removeIf(e -> id.equals(e.getId()));
        writeAll(storeName, all);
    }

    public <T> void writeAll(String storeName, List<T> items) {
        try {
            mapper.writerWithDefaultPrettyPrinter()
                .writeValue(dataDir.resolve(storeName + ".json").toFile(), items);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write local data store '" + storeName + "': " + e.getMessage(), e);
        }
    }

    public String generateId() {
        return UUID.randomUUID().toString();
    }
}
