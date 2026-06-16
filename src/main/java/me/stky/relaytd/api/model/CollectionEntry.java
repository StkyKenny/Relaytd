package me.stky.relaytd.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@EqualsAndHashCode
@Table(name = "collections")
public class CollectionEntry {

    @EmbeddedId
    private CollectionEntryID entryID;

    /*@NotNull
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "type", referencedColumnName = "type"),
            @JoinColumn(name = "subtype", referencedColumnName = "subtype"),
            @JoinColumn(name = "name", referencedColumnName = "name")
    })
    private AstreID astreID;*/

    @Embedded
    private AstreID astreID;

    private String qty;
    private String description;
    private String parent;
    private LocalDate acquisition_date;

    @PrePersist
    @PreUpdate
    private void normalize() {
        this.setEntryID(new CollectionEntryID(entryID.getId().trim(), entryID.getCollection().trim(), entryID.getVariant().trim()));
    }
}
