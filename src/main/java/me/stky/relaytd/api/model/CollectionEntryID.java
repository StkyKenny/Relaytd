package me.stky.relaytd.api.model;


import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Embeddable
public class CollectionEntryID {
    @Convert(disableConversion = true)
    @NotNull
    private String id;
    @Convert(disableConversion = true)
    @NotNull
    private String collection;
    @Convert(disableConversion = true)
    @NotNull
    private String variant;
}
