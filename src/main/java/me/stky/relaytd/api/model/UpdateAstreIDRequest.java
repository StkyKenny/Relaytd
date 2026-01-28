package me.stky.relaytd.api.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateAstreIDRequest {

    @NotNull
    private AstreID oldID;
    @NotNull
    private AstreID newID;
}
