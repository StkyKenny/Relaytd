package me.stky.relaytd.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@EqualsAndHashCode
@Table(name = "connections")
public class Astre {

    @NotNull
    @EmbeddedId
    private AstreID astreID;

    private String subname;
    private String tags;
    private String excluded_tags;
    private String link;
    private String description;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "type", column = @Column(name = "parent_type")),
            @AttributeOverride(name = "subtype", column = @Column(name = "parent_subtype")),
            @AttributeOverride(name = "name", column = @Column(name = "parent_name")),
    })
    private AstreID parentAstreID;
    private String parent; // Older implementation

    private String id;
    @Schema(hidden = true)
    private LocalDate date_added;
    @Schema(hidden = true)
    private LocalDate last_modified;
    private Boolean from_before; // this field indicate the date_added isn't representative of the date this astre was discovered

    /**
     * Deep copy
     *
     * @return a deep-copy of the current Astre
     */
    public Astre clone() {
        return new Astre(
                new AstreID(astreID.getType(), astreID.getSubtype(), astreID.getName()),
                subname, tags, excluded_tags, link, description, parentAstreID, parent, id, date_added, last_modified, from_before);
    }
}
