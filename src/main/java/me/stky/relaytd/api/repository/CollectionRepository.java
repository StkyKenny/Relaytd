package me.stky.relaytd.api.repository;

import me.stky.relaytd.api.model.CollectionEntry;
import me.stky.relaytd.api.model.CollectionEntryID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionRepository extends JpaRepository<CollectionEntry, CollectionEntryID> {

    @Query("SELECT a FROM CollectionEntry a WHERE a.entryID.collection = :collection_query")
    List<CollectionEntry> findByCollection(@Param("collection_query") String collection);

    @Query("SELECT a FROM CollectionEntry a " +
            "WHERE a.astreID.name = :name" +
            " AND a.astreID.type = :type" +
            " AND (" +
            "   (:subtype IS NULL AND a.astreID.subtype IS NULL)" + // Can be nullable because of older data structure
            " OR a.astreID.subtype = :subtype" +
            ")"
    )
    List<CollectionEntry> findBySource(@Param("name") String name, @Param("type") String type, @Param("subtype") String subtype);

}


