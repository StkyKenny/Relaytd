package me.stky.relaytd.api.service;

import me.stky.relaytd.api.model.Astre;
import me.stky.relaytd.api.model.AstreDTO;
import me.stky.relaytd.api.model.AstreID;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface AstreService {
    List<Astre> getAllAstre();

    Page<Astre> getPaginatedAstres(int pageNumber, int size, String sortBy, String order);

    Optional<Astre> getAstreById(AstreID astreID);

    Optional<Astre> saveAstre(Astre astre);

    Optional<Astre> updateAstre(AstreDTO astreDTO);

    Optional<Astre> updateAstreWithoutTimestamp(Astre astre);

    boolean deleteAstre(AstreID astreID);

    Optional<Astre> upsertAstre(AstreDTO astreDTO);

    List<Astre> upsertAstres(List<AstreDTO> astresDTO);


    Optional<Astre> updateAstreID(AstreID olderID, AstreID newID);
}
