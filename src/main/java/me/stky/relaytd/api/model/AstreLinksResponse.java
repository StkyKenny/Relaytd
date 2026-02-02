package me.stky.relaytd.api.model;

import java.util.List;


public record AstreLinksResponse(List<Astre> astres, List<AstreChildren> childrenList) {

}
