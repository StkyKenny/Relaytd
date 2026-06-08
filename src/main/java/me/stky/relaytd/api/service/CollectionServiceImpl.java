package me.stky.relaytd.api.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import me.stky.relaytd.api.model.*;
import me.stky.relaytd.api.repository.CollectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class CollectionServiceImpl implements CollectionService {

    private final CollectionRepository collectionRepository;

    @PersistenceContext
    private EntityManager entityManager;


    @Autowired
    public CollectionServiceImpl(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }

    @Override
    public List<CollectionEntry> getCollectionFromName(String collection) {
        return collectionRepository.findByCollection(collection);
    }

    @Override
    public List<CollectionEntry> getFromSource(@RequestBody AstreID astreID) {
        return collectionRepository.findBySource(astreID.getName(), astreID.getType(), astreID.getSubtype());
    }

    @Override
    public CollectionEntry changeAstreSource(UpdateAstreIDRequest updateAstreIDRequest) {
        return null;
    }

    /*@Override
    public CollectionEntry changeAstreSource(UpdateAstreIDRequest updateAstreIDRequest) {

        /*Objects.requireNonNull(oldID);
        Objects.requireNonNull(newID, "Newer ID can't be null");

        var astreDB = astreRepository.findById(oldID);
        if (astreDB.isEmpty() || astreRepository.findById(newID).isPresent()) {
            return Optional.empty();
        }
        Astre astreCopy = astreDB.get().clone();
        astreCopy.setAstreID(newID);
        astreCopy.setLast_modified(LocalDate.now());
        Astre newAstre = astreRepository.save(astreCopy);
        astreRepository.deleteById(oldID);
        return Optional.of(newAstre);
    }*/

    @Override
    public List<CollectionEntry> getAll() {
        return collectionRepository.findAll();

    }

    @Override
    public Optional<CollectionEntry> saveCollectionEntry(CollectionEntry entry, AstreID foreignKeyEntity) {

        System.out.println(entry);
        CollectionEntryID id = new CollectionEntryID(entry.getEntryID().getId(), entry.getEntryID().getCollection(), entry.getEntryID().getVariant());
        if (collectionRepository.findById(id).isEmpty()) {
            //AstreID astreRef = entityManager.getReference(AstreID.class, foreignKeyEntity);
            Astre astre = entityManager.find(Astre.class, foreignKeyEntity);

            entry.setAstreID(astre.getAstreID());

            return Optional.of(collectionRepository.save(entry));
        }
        return Optional.empty();
    }


}
