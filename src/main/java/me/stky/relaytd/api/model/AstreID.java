package me.stky.relaytd.api.model;


import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
// Not using @Setter because i want to force using trim() in each setter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
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

    public void setSubtype(String subtype) {
        this.subtype = subtype.trim();
    }

    public void setName(String name) {
        this.name = name.trim();
    }

    public void setType(String type) {
        this.type = type.trim();
    }

    public AstreID(String type, String subtype, String name) {
        this.type = type.trim();
        this.subtype = subtype.trim();
        this.name = name.trim();
    }

    public AstreID clone() {
        return new AstreID(this.getType(), this.getSubtype(), this.getName());
    }

}
