package me.stky.relaytd.api.model;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Embeddable
public class AstreID {
    @NotNull
    @Column(name = "type")
    private String type;
    @NotNull
    @Column(name = "subtype")
    private String subtype;
    @NotNull
    @Column(name = "name")
    private String name;
}
