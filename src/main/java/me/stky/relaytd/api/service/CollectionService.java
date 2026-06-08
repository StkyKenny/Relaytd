package me.stky.relaytd.api.service;

import me.stky.relaytd.api.model.AstreID;
import me.stky.relaytd.api.model.CollectionEntry;
import me.stky.relaytd.api.model.UpdateAstreIDRequest;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

public interface CollectionService {
    List<CollectionEntry> getCollectionFromName(String collection);

    List<CollectionEntry> getFromSource(@RequestBody AstreID astreID);

    CollectionEntry changeAstreSource(@RequestBody UpdateAstreIDRequest updateAstreIDRequest);

    Optional<CollectionEntry> saveCollectionEntry(CollectionEntry entry, AstreID foreignKeyEntity);

    List<CollectionEntry> getAll();
}
