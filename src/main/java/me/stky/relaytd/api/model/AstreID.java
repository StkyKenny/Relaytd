package me.stky.relaytd.api.model;


import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Embeddable
public class AstreID {
    @Convert(disableConversion = true)
    @Column(name = "type")
    private String type;
    @Convert(disableConversion = true)
    @Column(name = "subtype")
    private String subtype;
    @Convert(disableConversion = true)
    @Column(name = "name")
    private String name;

    public AstreID clone() {
        return new AstreID(this.getType(), this.getSubtype(), this.getName());
    }
}
